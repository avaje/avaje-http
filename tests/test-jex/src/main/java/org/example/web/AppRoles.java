package org.example.web;

import io.avaje.jex.security.Role;

public enum AppRoles implements Role {
  ANYONE,
  ADMIN,
  BASIC_USER
}
