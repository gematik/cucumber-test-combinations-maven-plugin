<img align="right" width="200" height="37" src="doc/images/Gematik_Logo_Flag.png" alt="gematik logo"/> <br/>

# Release notes

## 2.9.18

### Feature

- Reworked report about used groups. Only groups where all items are collected are mentioned now. Additionally, the used poolGroups are added and all used items (which can differ) 
- To execute scenarios will be counted and info added to count summary. Empty scenarios does not count

### Bugfix

- Used groups are now filled with the real data

## 2.9.16

### Feature

- If not only one item for requested group is found, even if the requested amount is set to 0, the plugin will stop and return an error.

### Bugfix

- Check goal also refers to accepted response codes now

## 2.9.15

### Features

- For all requests that are made to endpoints can be specified what response codes are valid and the response should be parsed. Read more [`docu`](./doc/userguide/GettingStarted.adoc)

## 2.9.14

### Bugfixes

- The plugin will no longer generate examples in a file that is anotated with @WIP or any other skipping tag 

## 2.9.13

### Features

- Summary how many executions are generated are now located at ./target/generated-combine [`docu`](./doc/userguide/GettingStarted.adoc)

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