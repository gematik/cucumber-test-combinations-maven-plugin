# language: de
Funktionalität:  TestFeature1

  Szenariogrundriss: ShouldBeFine1
    Angenommen Reservierung eines Test-Clients "<ClientName>" an der Schnittstelle "<HEADER_1>"
    Wenn       Registrierung des Test-Client "<ClientName>" mit "<BenutzerName>" beim TI-Messenger-Dienst
    Dann       Registrierung ist "<Status>"

    @Filter(!HEADER_1.properties["homeserver"].equals(HEADER_2.properties["homeserver"]))
    @Filter(HEADER_1.hasTag("client"))
    @DistinctColumn(HEADER_1)
    Beispiele:
      | HEADER_1 | HEADER_2 |
      | api1     | api2     |
      | api1     | api2     |
      | api1     | api2     |
      | api1     | api2     |

  Szenariogrundriss: ShouldBeFine2
    Angenommen Reservierung eines Test-Clients "<ClientName>" an der Schnittstelle "<HEADER_1>"
    Wenn       Registrierung des Test-Client "<ClientName>" mit "<BenutzerName>" beim TI-Messenger-Dienst
    Dann       Registrierung ist "<Status>"

    @Filter(HEADER_1.properties["homeserver"].equals(HEADER_2.properties["homeserver"]))
    Beispiele:
      | HEADER_1 | HEADER_2 |
      | api1     | api2     |
      | api1     | api2     |
      | api1     | api2     |

  Szenariogrundriss: ShouldBeFine3
    Angenommen Reservierung eines Test-Clients "<ClientName>" an der Schnittstelle "<HEADER_1>"
    Wenn       Registrierung des Test-Client "<ClientName>" mit "<BenutzerName>" beim TI-Messenger-Dienst
    Dann       Registrierung ist "<Status>"

    @Filter(!HEADER_1.properties["homeserver"].equals(HEADER_2.properties["homeserver"]))
    @Filter(HEADER_1.hasTag("participant"))
    Beispiele:
      | HEADER_1 | HEADER_2 |
      | api1     | api2     |
      | api1     | api2     |
      | api1     | api2     |
      | api1     | api2     |
      | api1     | api2     |
      | api1     | api2     |
      | api1     | api2     |

  Szenariogrundriss: ShouldBeFine4
    Angenommen Reservierung eines Test-Clients "<ClientName>" an der Schnittstelle "<HEADER_1>"
    Wenn       Registrierung des Test-Client "<ClientName>" mit "<BenutzerName>" beim TI-Messenger-Dienst
    Dann       Registrierung ist "<Status>"

    @Filter(!HEADER_1.properties["homeserver"].equals(HEADER_2.properties["homeserver"]))
    @Filter(HEADER_1.hasTag("participant"))
    Beispiele:
      | HEADER_1 | HEADER_2 |
      | api1     | api2     |
      | api1     | api2     |
      | api1     | api2     |
      | api1     | api2     |
      | api1     | api2     |
      | api1     | api2     |
      | api1     | api2     |
