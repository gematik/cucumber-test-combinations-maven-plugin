<img align="right" width="200" height="37" src="doc/images/Gematik_Logo_Flag.png" alt="gematik logo"/> <br/>

# Release notes

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