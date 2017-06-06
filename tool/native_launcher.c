/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */

/* Necessary for realpath() */
#define _XOPEN_SOURCE 500

#include <limits.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>

int main(int argc, char* argv[], char* envp[]) {
    if (argc < 1) {
        fprintf(stderr, "argc < 1\n");
        return EXIT_FAILURE;
    }

    char exec[PATH_MAX+3];
    if (realpath(argv[0], exec) == NULL) {
        perror("realpath");
        fprintf(stderr, "argv[0]=%s\n", argv[0]);
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
