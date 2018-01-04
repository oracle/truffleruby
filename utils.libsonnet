// File is formatted with
// `jsonnet fmt --indent 2 --max-blank-lines 2 --sort-imports --string-style s --comment-style s`

// Helper functions for CI definition. They only help with the structure
// and warn about broken rules. With few small modifications in final build
// field for comprehension they could be completely dropped and this file would
// still produce the same result.
{
  ensure_it_has:: function(included_parts, part, name)
    if std.count(included_parts, part) == 0
    then error 'part ' + part + ' has to be included before ' + name +
               ' but has only ' + included_parts
    else true,

  // It computes full name for each part (e.g. $.platform.linux) and adds field
  // `included_parts+: ['$.platform.linux']` into each part. When parts are
  // added together to form a build the array in `included_parts` composes and
  // keeps track of all parts used to compose the build.
  // The function returns part_definitions with the included_parts added to
  // each part.
  add_inclusion_tracking: function(part_definitions, prefix, contains_parts)
    {
      local content = part_definitions[k],
      local new_prefix = prefix + '.' + k,

      [k]: (
        if std.type(content) == 'object' && contains_parts
        then
          // process the part object
          content { included_parts+:
            // piggy back the build_has_to_already_have check here to avoid
            // extra field in the output
            local included_before_check =
              if std.objectHasAll(content, 'build_has_to_already_have')
              then
                std.foldl(
                  // make sure all required parts are already in there using super
                  function(r, p) r && $.ensure_it_has(super.included_parts, p, new_prefix),
                  content.build_has_to_already_have,
                  true
                )
              else true;
            // if check is ok just add the part's name into included_parts
            if included_before_check then [new_prefix] }
        else
          // look recursively for parts
          if std.type(content) == 'object'
          then $.add_inclusion_tracking(
            content,
            new_prefix,
            // assume parts are always nested 1 level in a group
            true
          )
          else content
      )
      for k in std.objectFields(part_definitions)
    },

  // Ensures that no part is included twice using `included_parts` field.
  included_once_check: function(builds)
    std.map(
      function(build)
        local name = if std.objectHas(build, 'name') then build.name else build;
        // collect repeated parts
        local repeated = std.foldl(function(r, i)
                                     if std.count(build.included_parts, i) == 1
                                     then r else r + [i],
                                   build.included_parts,
                                   []);
        if std.length(repeated) == 0
        then build
        else error 'Parts ' + repeated +
                   ' are used more than once in build: ' + name +
                   '. See for duplicates: ' + build.included_parts,
      builds
    ),

  // Restrict builds to a list given in restriction
  restrict_to: function(restriction, builds)
    if std.length(restriction) == 0
    then builds
    else std.filter(function(b) std.count(restriction, b.name) > 0, builds),

  decorate_and_check_builds: function(restrict_to, builds)
    $.restrict_to(restrict_to, $.included_once_check(
      [
        build {
          environment+: {
            // Add PARTS_INCLUDED_IN_BUILD env var so the parts used
            // in the build are printed in its log
            PARTS_INCLUDED_IN_BUILD:
              std.join(
                ', ',
                std.map(
                  // there is no way to escape $ afaik, stripping it instead,
                  // otherwise it is interpreted as variable
                  function(name) std.substr(name, 2, std.length(name) - 2),
                  build.included_parts
                )
              ),
          },
        }
        for build in builds
      ]
    )),

  // Try to read a field, if not present print error including the name
  // of the object
  debug_read: function(obj, field)
    if std.objectHasAll(obj, field)
    then obj[field]
    else error 'missing field: ' + field + ' in ' + obj.name,

  index: function(arr, obj)
    std.filter(function(i) obj == arr[i],
               std.range(0, std.length(arr) - 1)),
}
