package org.example.myapp.web;

import io.javalin.security.RouteRole;

public enum AppRoles implements RouteRole {
  ANYONE, ADMIN, BASIC_USER, ORG_ADMIN
}
