# language: de
Funktionalität:  AF_10057 - Anmeldung eines Akteurs am Messenger-Service
  Mit diesem Anwendungsfall meldet sich ein Akteur an einem Messenger-Service an und registriert seinen
  TI-Messenger-Client als Endgerät.

  Szenariogrundriss: Anmeldung eines Akteurs
    Angenommen Reservierung eines Test-Clients "<ClientName>" an der Schnittstelle "<ApiName_1>"
    Wenn       Registrierung des Test-Client "<ApiName_2>" mit "<BenutzerName>" beim TI-Messenger-Dienst
    Dann       Registrierung ist "<Status>"

    @Filter(ApiName_1.hasTag("orgAdmin"))
    @Filter(ApiName_2.hasTag("client"))
    @Filter(ApiName_1.properties["homeserver"].equals(ApiName_2.properties["homeserver"]))
    @AllowDoubleLineUp(false)
    @AllowSelfCombine(false)
    @MinimalTable
    Beispiele:
      | ApiName_1 | ApiName_2 |
