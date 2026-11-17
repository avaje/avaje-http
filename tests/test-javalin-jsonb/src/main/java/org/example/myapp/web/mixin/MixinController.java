package org.example.myapp.web.mixin;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Path;
import io.avaje.http.api.Post;

@Path("mixin")
@Controller
public class MixinController {

  @Post
  int createNew(MixinDto data) {
    return 0;
  }
}
