<img align="right" width="200" height="37" src="doc/images/Gematik_Logo_Flag.png" alt="gematik logo"/> <br/>

# Release notes

## 2.11.2

## Features

- Ignore feature files that are fully skipped by the generator

## 2.11.1

- Rollback to java version 11

## 2.11.0

### Breaking Changes

- The templateDir option has been renamed to templateSources

### Feature

- The templateSources option accepts a comma separated list of directories to search for templates. The directories are
  processed in the order they are given. If a template file with the same name is found in multiple directories, an error is raised.

## 2.10.0

### Feature

- A new flag for prepare allows undefined tags and properties (see [`docu`](./doc/userguide/GettingStarted.adoc))

## 2.9.20

### Bugfix

- Subfolders in the templateDir will no longer rise an AccessDeniedException

## 2.9.19

### Feature

- The environment variables () written in the environment variables can now be formatted in html style for better use

## 2.9.18

### Feature

- Reworked report about used groups. Only groups where all items are collected are mentioned now. Additionally, the used
  poolGroups are added and all used items (which can differ)
- To execute scenarios will be counted and info added to count summary. Empty scenarios does not count

### Bugfix

- Used groups are now filled with the real data

## 2.9.16

### Feature

- If not only one item for requested group is found, even if the requested amount is set to 0, the plugin will stop and
  return an error.

### Bugfix

- Check goal also refers to accepted response codes now

## 2.9.15

### Features

- For all requests that are made to endpoints can be specified what response codes are valid and the response should be
  parsed. Read more [`docu`](./doc/userguide/GettingStarted.adoc)

## 2.9.14

### Bugfixes

- The plugin will no longer generate examples in a file that is anotated with @WIP or any other skipping tag

## 2.9.13

### Features

- Summary how many executions are generated are now located at
  ./target/generated-combine [`docu`](./doc/userguide/GettingStarted.adoc)

## 2.9.12

### Features

- SoftFilter Feature implemented

## 2.9.11

### Features

- Allows setting a proxy for all api calls

## 2.9.9

### Features

- Allows TLS version 1.2 & 1.3

## 2.9.8

### Features

- Improved error handling for api calls and item context errors
- New parameters to continue all plugin goal executions on context and request errors

## 2.9.0

### Features

- New goal check api implemented. It is possible to request an endpoint and check for some
  conditions
- Improved error message if connection to api failed
- New parameters to disable explicit executions or all executions at once implemented

### Bugs

- Check-execution removes double `/` from url

## 2.8.0

### Features

- Pooling - The different items can be grouped and be selected via poolGroups
- Used groups available in file and at runtime in environment

## 2.6.0

### Features

- Filtern nach Versionen mit @Version
- eigene Tag-Kategorie für Versions-Filter
- globale preset-Filter unter *_\<projectFilter\>_* setzbar