package processing.utils;

public class PreferencesException extends Exception {
  private String title;
  private String message;
  private boolean fatal;

  public PreferencesException(String title, String message, boolean fatal){
    this.title = title;
    this.message = message;
    this.fatal = fatal;
  }


  @Override
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public boolean isFatal() {
    return fatal;
  }

  public void setFatal(boolean fatal) {
    this.fatal = fatal;
  }
}
