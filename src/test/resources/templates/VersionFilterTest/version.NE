# language: de
Funktionalität:  Testfile
  Szenariogrundriss: Only one test
    Angenommen something happens

    @Version(ApiName1A--NE--1.0.11)

    Beispiele:
      | ApiName1A |

  Szenariogrundriss: Something happens just a bit other way
    Angenommen something happens

    @Version(ApiName1A,ApiName1B--NE--1.0.11)

    Beispiele:
      | ApiName1A | ApiName1B |