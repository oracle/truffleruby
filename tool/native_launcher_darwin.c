/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */

#include <limits.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>

#include <mach-o/dyld.h>

int main(int argc, char* argv[], char* envp[]) {
    if (argc < 1) {
        fprintf(stderr, "argc < 1\n");
        return EXIT_FAILURE;
    }

    char *self = "";
    uint32_t bufsize = 0;

    /* Get the buffer size */
    int ret = _NSGetExecutablePath(self, &bufsize);
    if (bufsize == 0) {
      fprintf(stderr, "_NSGetExecutablePath failed to give the buffer size\n");
      return EXIT_FAILURE;
    }

    /* Get the path of the current executable */
    self = malloc(bufsize);
    ret = _NSGetExecutablePath(self, &bufsize);
    if (ret != 0) {
      fprintf(stderr, "_NSGetExecutablePath returned %d\n", ret);
      return EXIT_FAILURE;
    }

    char exec[PATH_MAX+3];
    if (realpath(self, exec) == NULL) {
        perror("realpath");
        fprintf(stderr, "self=%s\n", self);
        return EXIT_FAILURE;
    }

    size_t len = strlen(exec);
    exec[len++] = '.';
    exec[len++] = 's';
    exec[len++] = 'h';
    exec[len] = '\0';

    char** args = argv;
    args[0] = exec;

    execve(exec, args, envp);

    /* execve only returns on failure */
    perror("execve");
    fprintf(stderr, "%s\n", exec);
    return EXIT_FAILURE;
}
