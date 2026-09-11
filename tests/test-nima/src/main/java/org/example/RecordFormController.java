package org.example;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Form;
import io.avaje.http.api.Post;
import io.avaje.http.api.Produces;

@Controller
public class RecordFormController {

  @Produces("text/plain")
  @Post("recordform")
  String submit(@Form CustomerForm form) {
    return form.firstName()
      + "|" + form.lastName()
      + "|" + form.invoice().street() + "," + form.invoice().city();
  }
}
