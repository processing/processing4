# Contributing to Processing on GitHub

Welcome to the contributor guidelines! 

This document is for new contributors looking to contribute code to Processing, contributors refreshing their memory on some technical steps, or anyone interested in working on Processing’s codebase. We believe that anyone can be a contributor. You don’t need to be an expert. We also know that not everyone has the same time, energy, or resources to spend on Processing. That’s okay. We’re glad you’re here!

> [!TIP]
> For questions about your own sketches, or broader conversations about coding in Processing, our [online forum](https://discourse.processing.org/) is a fantastic resource (make sure to read the [forum guidelines](https://discourse.processing.org/t/welcome-to-the-processing-foundation-discourse/8) before posting). You can also visit the [Processing Community Discord](https://discord.gg/8pFwMVATh8).

## About GitHub
Processing’s codebase is hosted on [GitHub](https://github.com/processing). GitHub is a website where people can collaborate on code. It’s widely used for open source projects and makes it easier to keep track of changes, report issues with the software, and contribute improvements to the code.

If you're new to GitHub, a good place to start is [this tutorial](https://github.com/firstcontributions/first-contributions/blob/main/docs/gui-tool-tutorials/github-desktop-tutorial.md) guide, which walks you through the basics of contributing to a project using GitHub Desktop. For more information, we recommend [Git and GitHub for Poets](https://www.youtube.com/playlist?list=PLRqwX-V7Uu6ZF9C0YMKuns9sLDzK6zoiV), a beginner-friendly video series by Dan Shiffman.

## About issues

Most activity on Processing’s GitHub happens in _issues_. Issues are GitHub posts which can contain bug reports, feature requests, or broader discussions about the development of Processing. It’s a great place to begin contributing.

To file a new issue, visit the [Issues](https://github.com/processing/processing4/issues) tab on the repository and click `New issue` then select the most appropriate template and follow the included instructions. These templates help maintainers understand and respond to issues faster.

## Working on the Processing codebase

### Prerequisites

To contribute to Processing, we recommend using [GitHub Desktop](https://github.com/apps/desktop) and [IntelliJ IDEA (Community Edition)](https://www.jetbrains.com/idea/download/), as that’s the toolchain we’re best able to support. If you’re comfortable using Git on the command line or prefer a different editor, that’s totally fine too! Use what works best for you. Some familiarity with the [command line](https://developer.mozilla.org/en-US/docs/Learn_web_development/Getting_started/Environment_setup/Command_line) can help, but it’s not required.

You'll need to set up a local development environment—see our [build instructions](https://github.com/processing/processing4/blob/main/BUILD.md) to get started.

### Making your first contribution

Most issues marked [help wanted](https://github.com/processing/processing4/issues?q=is%3Aissue+is%3Aopen+label%3A%22help+wanted%22) or [good first issue](https://github.com/processing/processing4/issues?q=is%3Aissue%20is%3Aopen%20label%3A%22good%20first%20issue%22%20) are a good place to start. 

Before beginning work on a code contribution, please make sure that:

-   The issue has been discussed and a proposed solution has been approved.
-   You have been **assigned** to the issue.
    
If an implementation has been agreed upon but no one has volunteered to take it on, feel free to comment and offer to help. A maintainer can then assign the issue to you.

Please do **not** open a pull request for an issue that is already assigned to someone else. We follow a “first assigned, first served” approach to avoid duplicated work. If you open a PR for an issue that someone else is already working on, your PR will be closed.

If an issue has been inactive for a long time, you’re welcome to check in politely by commenting to see if the assignee still plans to work on it or would be open to someone else taking over.

There’s no hard deadline for completing contributions. We understand that people often contribute on a volunteer basis and timelines may vary. That said, if you run into trouble or have questions at any point, don’t hesitate to ask for help in the issue thread. Maintainers and other community members are here to support you.

### Follow the style guidelines
Keep the [style guidelines](https://github.com/processing/processing/wiki/Style-Guidelines) in mind when making changes to the code. If you don’t, someone else will have to reformat your code so that it fits everything else (or we’ll have to reject your changes if it’ll take us too long to clean things up).

### Test locally
Before you contribute your changes, it's essential that you make sure that Processing still builds, runs, and functions on your machine. Here again, the [build instructions](https://github.com/processing/processing4/blob/main/BUILD.md) are your best friend. Pay special attention to any features that may be affected by your changes. Does everything still work as before? Great!

## Submit a pull request (PR)

Once your changes are ready:

1.  Push your branch to your fork
2.  Open a pull request from your branch into `main` on the official repository
3.  Fill out the pull request information:

   -   **Title**: clear and descriptive
   -   **Resolves**: add `Resolves #[issue-number]` if applicable
   -   **Changes**: explain what you changed and why
   -   **Tests**: mention if you added tests or validated your changes
   -   **Checklist**: ensure tests pass and the branch is up-to-date

Maintainers usually review pull requests within one to two weeks. If changes are requested, follow up by pushing additional commits. The PR will automatically update.

If there hasn’t been any activity after two weeks, feel free to gently follow up. We kindly ask that you don’t request a review or tag maintainers before that time. Thanks for your patience!

Before opening a pull request, please make sure to discuss the related issue and get assigned to it first. This helps us stay aligned and avoid unnecessary work. Thank you!

## Adding New Features to Processing

If you have an idea for something Processing doesn’t yet support, **creating a library** is often the best way to contribute.  The [Processing Library Template](https://github.com/processing/processing-library-template) provides a starting point for developing, packaging, and distributing Processing libraries. For more instructions, see the [library template documentation](https://processing.github.io/processing-library-template/).

Once your library is complete, you can submit it to the official [Processing Contributions](https://github.com/processing/processing-contributions) repository. This makes it discoverable through the PDE’s Contribution Manager. Follow the guidelines in the [Processing Library Guidelines](https://github.com/processing/processing4/wiki/Library-Guidelines).

### Libraries as the Starting Point for Features
Nearly all new features are first introduced as a Library or a Mode, or even as an example. The current [OpenGL renderer](http://glgraphics.sourceforge.net/) and Video library began as separate projects by Andrés Colubri, who needed a more performant, more sophisticated version of what we had in Processing for work that he was creating. The original `loadShape()` implementation came from the “Candy” library by Michael Chang (“mflux“). Similarly, Tweak Mode began as a [separate project](http://galsasson.com/tweakmode/) by Gal Sasson before being incorporated. PDE X was a Google Summer of code [project](https://github.com/processing/processing-experimental) by Manindra Moharana that updated the PDE to include basic refactoring and better error checking.

### Why Develop Outside the Core?
Working outside the main Processing codebase has several advantages:

* It’s easier for the contributor to develop the software without it needing to work for tens or hundreds of thousands of Processing users.
* It provides a way to get feedback on that code independently of everything else, and the ability to iterate on it rapidly.
* This feedback process also helps gauge the level of interest for the community, and how it should be prioritized for the software.
* We can delay the process of “normalizing” the features so that they’re consistent with the rest of Processing (function naming, structure, etc).

### What Guides the Inclusion of New Features?
We take maintenance seriously. A feature added to the core becomes a long-term responsibility. If it breaks, it needs fixing. Sometimes the original developer is no longer active (which is normal), and the burden falls on others.

Processing is a massive project that has existed for more than 20 years. Part of its longevity comes from the effort that’s gone into keeping things as simple as we can, and in particular, making a lot of difficult decisions about *what to leave out*. 

Adding a new feature always has to be weighed against the potential confusion of one more thing—whether it’s a menu item, a dialog box, a function that needs to be added to the reference, etc. Adding a new graphics function means making it work across all the renderers that we ship (Java2D, OpenGL, JavaFX, PDF, etc) and across platforms (macOS, Windows, Linux).

It may also mean new interface elements, updates to the reference, and more documentation.

So when we consider a new feature, we ask ourselves:

> Does this solve a problem for many users? Is it worth the added complexity and extra maintenance work?

These are not easy decisions, especially when volunteers are offering their time and ideas. But we have to make them carefully to keep Processing sustainable.

## Editor

The current Editor component is based on the ancient [JEditSyntax](http://syntax.jedit.org/) package and has held up long past its expiration date. [Exhaustive work](https://github.com/processing/processing4/blob/master/app/src/processing/app/syntax/README.md) has been done to look at replacing the component with something more modern, like `RSyntaxArea`, but that approach was considered too risky.

With Processing 4.4.0, we’ve started transitioning the Processing UI from Swing to Jetpack Compose Multiplatform, allowing us to replace Swing components gradually, without a full rewrite. Any work on updating the PDE and adding new features should focus on this migration. Replacing JEditSyntax will likely be the last step in the process. In the meantime, the editor does what it needs to do, for the intended audience. Features like code-folding, refactoring, or symbol navigation are currently out of scope.

For users who want editor features beyond what the PDE offers, we’re working to make Processing easier to use in other environments. [Migrating the Processing CLI to Gradle](https://github.com/orgs/processing/projects/32/views/2?filterQuery=CLI&pane=issue&itemId=81026317) and [better Language Server support](https://github.com/orgs/processing/projects/32/views/2?filterQuery=LSP&pane=issue&itemId=90809690) will help make that possible. This should reduce the pressure to add these features to the PDE itself, allowing it to stay focused on being a minimal, beginner-friendly coding sketchbook. If you'd like to help, [let us know](https://github.com/processing/processing4/issues/883)!

## Refactoring

Refactoring is fun! There’s always more cleaning to do. It’s also often not very helpful.

Broadly speaking, the code is built the way it is for a reason. There are so many things that can be improved, but those improvements need to come from an understanding of what’s been built so far. Changes that include refactoring are typically only accepted from contributors who have an established record of working on the code. With a better understanding of the software, the refactoring decisions come from a better, more useful place.

## Contributor Recognition

The Processing project follows the [all-contributors](https://github.com/kentcdodds/all-contributors) specification, recognizing all types of contributions, not just code. We use the @all-contributors bot to handle adding people to the README.md file. You can ask the @all-contributors bot to add you in an issue or PR comment like so:

```
@all-contributors please add @[your GitHub handle] for [your contribution type]
```

We usually add contributors automatically after merging a PR, but feel free to request addition yourself by commenting on [this issue](https://github.com/processing/processing4-carbon-aug-19/issues/839).

## Other Ways to Contribute

We're always grateful for your help fixing bugs and implementing new features BUT You don’t have to write code to contribute to Processing! Here are just a few other ways to get involved:

-   **Translation** – Help localize the software and documentation in your language. Many of us made our first contribution this way.
-   **Testing** – Try out new releases (especially the betas) and [report bugs](https://github.com/processing/processing4/issues/new/choose).
-   **Documentation** – Improve tutorials, reference pages, or even this guide!
-   **Design** – Contribute UI design ideas or help improve user experience.
-   **Community Support** – Answer questions on the [forum](https://discourse.processing.org/).
-   **Education** – Create learning resources, curriculums, organize workshops, or share your teaching experiences.
-   **Art and Projects** – Share what you’re making with Processing and use the #BuiltWithProcessing hashtag 💙
-   **Outreach and Advocacy** – Help others discover and get excited about the project.
