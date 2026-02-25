#!/bin/bash

set -euo pipefail
trap 'cleanup' EXIT


# Global variables
METHOD=""                     # user-requested method (snap|flatpak|aur|deb|nix|tarball)
DRY_RUN=false
AUTO_YES=false
TEMP_DIR=""
SCRIPT_NAME="$(basename "$0")"

# Default .deb URL
DEB_URL="https://processing.org/download/processing-latest-amd64.deb"   # placeholder


# Helper functions
usage() {
    cat <<EOF
Usage: $SCRIPT_NAME [options]

Options:
  --method snap|flatpak|aur|deb|nix|tarball   Force a specific installation method
  --dry-run                                    Show actions without executing
  --yes, -y                                    Automatic yes to prompts
  --list-methods                               List available installation methods and exit
  --help                                        Show this help message
EOF
    exit 0
}

list_methods() {
    echo "Available installation methods:"
    echo "  snap     – Snap package (auto-updating)"
    echo "  flatpak  – Flatpak from Flathub (auto-updating)"
    echo "  aur      – Arch User Repository (community package)"
    echo "  deb      – Direct .deb download (Debian/Ubuntu native)"
    echo "  nix      – Nix package (NixOS / Nix package manager)"
    echo "  tarball  – Manual download (fallback)"
    exit 0
}

error() {
    echo "Error: $*" >&2
    exit 1
}

confirm() {
    if [ "$AUTO_YES" = true ]; then
        return 0
    fi
    local prompt="$1"
    local response
    read -r -p "$prompt [Y/n] " response || true
    case "$response" in
        [nN][oO]|[nN]) return 1 ;;
        *) return 0 ;;
    esac
}

run() {
    if [ "$DRY_RUN" = true ]; then
        echo "[DRY RUN] $*"
    else
        "$@"
    fi
}

command_exists() {
    command -v "$1" >/dev/null 2>&1
}

sudo_run() {
    if [ "$DRY_RUN" = true ]; then
        echo "[DRY RUN] sudo $*"
        return 0
    fi
    if command_exists sudo; then
        sudo "$@"
    else
        error "sudo is required but not available."
    fi
}

cleanup() {
    if [ -n "$TEMP_DIR" ] && [ -d "$TEMP_DIR" ]; then
        rm -rf "$TEMP_DIR"
    fi
}

# Check if Processing is already installed
check_existing() {
    if command_exists processing || command_exists Processing; then
        echo "Processing appears to already be installed."
        if ! confirm "Continue with installation anyway?"; then
            echo "Installation aborted."
            exit 0
        fi
    fi
}


# Installation method functions

install_snap() {
    check_existing
    echo "Installing Processing via Snap..."
    if confirm "This will run 'sudo snap install processing --classic'. Continue?"; then
        run sudo snap install processing --classic
        echo "Snap installation complete."
    else
        echo "Aborted."
    fi
}

install_flatpak() {
    check_existing
    echo "Installing Processing via Flatpak..."
    if confirm "This will run 'flatpak install flathub org.processing.processingide'. Continue?"; then
        run flatpak install flathub org.processing.processingide
        echo "Flatpak installation complete."
    else
        echo "Aborted."
    fi
}

install_aur() {
    check_existing
    echo "Installing Processing from AUR..."

    local helper=""
    if command_exists yay; then
        helper="yay"
    elif command_exists paru; then
        helper="paru"
    fi

    if [ -n "$helper" ]; then
        if confirm "This will run '$helper -S processing'. Continue?"; then
            run "$helper" -S processing
            echo "AUR installation complete."
        else
            echo "Aborted."
        fi
    else
        echo "No AUR helper found. Install manually from AUR:"
        echo "  git clone https://aur.archlinux.org/processing.git"
        echo "  cd processing && makepkg -si"
    fi
}

install_deb_direct() {
    check_existing
    echo "Installing Processing via direct .deb download..."
    local deb_file="$TEMP_DIR/processing.deb"
    if confirm "This will download and install the latest .deb package. Continue?"; then
        run curl -L -o "$deb_file" "$DEB_URL"
        run sudo dpkg -i "$deb_file"
        run sudo apt-get install -f -y
        echo "Debian package installation complete."
    else
        echo "Aborted."
    fi
}

install_nix() {
    check_existing
    echo "Installing Processing via Nix..."
    if ! command_exists nix; then
        echo "Nix package manager not found. Please install Nix first: https://nixos.org/download/"
        return 1
    fi
    if confirm "This will run 'nix profile install nixpkgs#processing'. Continue?"; then
        run nix profile install nixpkgs#processing
        echo "Nix installation complete. The binary is available as 'Processing' (or 'processing' via symlink)."
    else
        echo "Aborted."
    fi
}

install_tarball() {
    check_existing
    echo "Installing Processing via direct download..."
    echo "This feature is coming soon! For now, you can manually download from:"
    echo "  https://processing.org/download/"
}


# Distribution detection helpers

is_debian_based() {
    if [ -f /etc/debian_version ]; then
        return 0
    fi
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        case "${ID:-}" in
            debian|ubuntu|linuxmint|pop) return 0 ;;
        esac
    fi
    return 1
}

is_arch_based() {
    if [ -f /etc/arch-release ]; then
        return 0
    fi
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        case "${ID:-}" in
            arch|manjaro|endeavouros) return 0 ;;
        esac
    fi
    return 1
}

is_nixos() {
    if [ -f /etc/NIXOS ] || command_exists nixos-version; then
        return 0
    fi
    return 1
}

has_flathub() {
    command_exists flatpak || return 1
    flatpak remote-list 2>/dev/null | grep -q flathub
}

has_snap() {
    command_exists snap
}


# Main

TEMP_DIR="$(mktemp -d)"

# Parse command-line arguments
while [ $# -gt 0 ]; do
    case "$1" in
        --method)
            [ $# -ge 2 ] || error "--method requires an argument"
            METHOD="$2"
            shift 2
            ;;
        --dry-run)
            DRY_RUN=true
            shift
            ;;
        --yes|-y)
            AUTO_YES=true
            shift
            ;;
        --list-methods)
            list_methods
            ;;
        --help)
            usage
            ;;
        *)
            error "Unknown option: $1"
            ;;
    esac
done

# If override method is provided, use it directly
if [ -n "$METHOD" ]; then
    case "$METHOD" in
        snap)     install_snap ;;
        flatpak)  install_flatpak ;;
        aur)      install_aur ;;
        deb)      install_deb_direct ;;
        nix)      install_nix ;;
        tarball)  install_tarball ;;
        *)        error "Invalid method: $METHOD. Use snap, flatpak, aur, deb, nix, or tarball." ;;
    esac
    exit 0
fi


# Auto-detection – try most native first

echo "Detecting best installation method for your system..."

# NixOS
if is_nixos; then
    echo "NixOS detected."
    if confirm "Install Processing via Nix (official package)?"; then
        install_nix
        exit 0
    fi
fi

# Debian/Ubuntu – prefer .deb over snap/flatpak
if is_debian_based; then
    echo "Debian/Ubuntu-based system detected."
    if confirm "Install Processing via .deb package (native)?"; then
        install_deb_direct
        exit 0
    fi
fi

# Arch Linux – AUR
if is_arch_based; then
    echo "Arch-based distribution detected."
    if command_exists yay || command_exists paru; then
        if confirm "Install Processing from AUR (community package)?"; then
            install_aur
            exit 0
        fi
    else
        echo "No AUR helper found. You can install manually from AUR:"
        echo "  git clone https://aur.archlinux.org/processing.git"
        echo "  cd processing && makepkg -si"
        exit 0
    fi
fi

# Snap (universal, auto-updating)
if has_snap; then
    echo "Snap is available."
    if confirm "Install Processing via Snap (auto-updating)?"; then
        install_snap
        exit 0
    fi
fi

# Flatpak (universal, auto-updating)
if has_flathub; then
    echo "Flatpak (Flathub) is available."
    if confirm "Install Processing via Flatpak (auto-updating)?"; then
        install_flatpak
        exit 0
    fi
fi

# Fallback
echo "No suitable package manager found. Falling back to direct download."
install_tarball
exit 0