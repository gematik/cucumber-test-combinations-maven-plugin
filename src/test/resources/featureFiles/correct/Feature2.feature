# language: de
Funktionalität:  TestFeature2

  Szenariogrundriss: Anmeldung eines Akteurs
    Angenommen Reservierung eines Test-Clients "<ClientName>" an der Schnittstelle "<HEADER_1>"
    Wenn       Registrierung des Test-Client "<ClientName>" mit "<BenutzerName>" beim TI-Messenger-Dienst
    Dann       Registrierung ist "<Status>"

    @DistinctColumn(HEADER_1)
    @AllowSelfCombine(false)
    @Filter(HEADER_1.hasTag("orgAdmin"))
    @Filter(HEADER_2.hasTag("client"))
    Beispiele:
      | HEADER_1 | HEADER_2 |
      | api1     | api2     |
      | api1     | api2     |
      | api1     | api2     |
      | api1     | api2     |
      | api1     | api2     |
      | api1     | api2     |
      | api1     | api2     |
      | api1     | api2     |
      | api1     | api2     |
      | api1     | api2     |
      | api1     | api2     |
      | api1     | api2     |
