class CompError extends Error {
  public String msg;

  CompError(String error) {
    msg = error;
  }
}
