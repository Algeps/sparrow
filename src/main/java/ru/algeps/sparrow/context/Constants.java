package ru.algeps.sparrow.context;

import java.nio.charset.StandardCharsets;

public final class Constants {
  public static final String VERSION = "0.0.3";
  public static final String SERVER_NAME = "Sparrow(%s)".formatted(VERSION);
  public static final String LOGO =
      """
                        ________
                      /         \\
                     |       *   \\
                     \\          / \\     ======
                     /\\ _______/__/    ||
            _____________      /       ||
            \\          /      /         ======   ======   ======  || //== || //==  ====  \\\\   //\\   //
             \\        /      /               || ||    ||  _____|| ||//    ||//    //  \\\\  \\\\ // \\\\ //
               \\     /      /                || ||    || ||    || ||      ||      \\\\  //   \\\\/   \\\\/
                 \\__/      /            ======   ======   ======  ||      ||       ====     ==    ==
                   |______/_____________________||___________________________________________________________
                                                ||                                             by Algeps
                                                ||                                             Version: %s
            """
          .formatted(VERSION);

  public static final byte[] NOT_FOUND_HTML_CONTENT =
      """
            <!DOCTYPE html>
            <html lang="en">
            <head>
            <title>404 Not found</title>
            </head>
            <body>
            <div align="center">
            <h1>404 Not found</h1>
            <hr style="width: 250px; height: 2px; background-color: #333;">
            <p align="center">%s</p>
            </div>
            </body>
            </html>"""
          .formatted(SERVER_NAME)
          .getBytes(StandardCharsets.UTF_8);

  private Constants() {}
}
