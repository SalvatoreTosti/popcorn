# assignment5

Movie Database Manager
This program is designed for use in a video rental store.
It has the following capabilities:
  Rent movies
  Return movies
  Remove movies
  Add new movies to the database
  Add additional copies of movies in database
  Find movies by ID or Title
  Adjust price of movies in database

## Usage

If leingen is installed on your system, you can activate the program using 'lein run' from within this directory.
If leingen is not installed, running the following command on the 'standalone' jar should run the program.
    $ java -jar assignment5-0.1.0-standalone.jar [args]

## Options

There is an initial resources/database.txt supplied, 
but this can be removed and then allow the user to create their own database from scratch via the program.

##Bugs

1. table-x does not properly sort ID's after 10, when double digits appear it does not sort in ascending or descending order correctly.
     Note: Converting ID's to strings / double did not alter behavior.
2. When entering a number for a price, or otherwise, prefacing the number with a '#' results in an improperly caught error.

## License

Copyright © 2014 Salvatore Tosti

Distributed under the Artistic License version 2.0
