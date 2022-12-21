# 

![logo](doc/images/Gematik_Logo_Flag.png)

# Cucumber-Test-Combinations-Maven-Plugin

## About The Project

The Cucumber-Test-Combinations-Maven-Plugin or CuTest-Combinations-Plugin fills empty examples tables in cucumber feature files with combinations of predefined values. The generated combinations can be configured by various filters.

![plugin](doc/images/plugin.png)

### Release Notes

See [ReleaseNotes](ReleaseNotes.md) for all information regarding the (newest) releases.

## Usage

Add the following to you `pom.xml` and configure the plugin as you need:

    <plugin>
      <groupId>de.gematik</groupId>
      <artifactId>cucumber-test-combinations-maven-plugin</artifactId>
      <version>2.5.0</version>
    </plugin>

To use the plugin you have to do two things:

1.  provide "combine items" (the values which will be filled in the empty tables) as JSON file with this structure:

        [ 
          {
            "value": "myValue", 
            "tags": ["myTag"], 
            "properties": { 
                "myProp": "A"
            }
          }
        ]

    -   The file contains a list of items. You can declare as many items as you want.

    -   Each item needs a value. This value will be later inserted in the tables.

    -   Items can have tags. Tags are just Strings associated with an item and can be used by filters
        to manipulate the combination generation.

    -   Items can have properties. Properties are key-value pairs associated with an item and can be used by filters
        to manipulate the combination generation.

2.  configure the empty tables in your feature files with tags that represent your constraints

        Feature:  example feature
         Scenario Outline:
          When you do something with <column_1> and <column_2>
          Then something happens

            @Filter(column_1.hasTag("myTag")) 
            @Filter(column_1.properties["myProp"].equals(column_2.properties["myProp"])) 
            Examples:
              | column_1 | column_2 | 

    -   The plugin will only use values with the tag `myTag` to fill the column\_1.

    -   The plugin will only generate combinations for this table where the values in column\_1 and column\_2 have the same property `myProp`

    -   An empty examples table with header names. You can access the table columns in the filter tags over their header names.

For in-depth documentation of the configuration, please refer to [GettingStarted](doc/userguide/GettingStarted.adoc).

## Contributing

If you want to contribute, please check our [CONTRIBUTING.md](CONTRIBUTING.md).

## License

Copyright \[yyyy\] gematik GmbH

Licensed under the **Apache License, Version 2.0** (the "License"); you may not use this file except in compliance with the License.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the [LICENSE](./LICENSE) for the specif ic language governing permissions and limitations under the License.
