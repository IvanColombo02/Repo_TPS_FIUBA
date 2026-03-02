import unittest
from tp2 import *

RED = "\033[91m"
GREEN = "\033[92m"
RESET = "\033[0m"

class TestTp2(unittest.TestCase):
    def ejecutar_test(self, archivo_cadenas, archivo_palabras, son_none = {}):
        """
        Test auxiliar para ejecutar tests de volúmen.
        """
        cadenas = procesar_archivo(archivo_cadenas)
        palabras = set(procesar_archivo(archivo_palabras))
        
        for i in range(len(cadenas)):
            solucion = validar_mensaje(cadenas[i], palabras)
            if i in son_none:
                self.assertEqual(solucion, MSG_NO_ES)
            else:
                self.assertNotEqual(solucion, MSG_NO_ES)
                self.assertGreater(len(solucion), 1)

    def test_todos_usados(self):
        """
            Testea qué sucede cuando el arreglo usa todas las palabras del
            diccionario.
        """
        cadena = "cadenadepruebaparaeltp2"
        palabras = {"prueba", "tp2", "cadena", "el", "para", "de"}
        solucion = validar_mensaje(cadena, palabras, 2, 6)
        self.assertIsNotNone(solucion)
        self.assertEqual(len(solucion.split()), 6)

    def test_sobran_letras(self):
        """
            Testea qué sucede cuando no se puede matchear toda la cadena.
        """
        cadena = "cadenadepruebaparaeltp2"
        palabras = {"prueba", "tp2", "cadena", "el", "para"}
        solucion = validar_mensaje(cadena, palabras)
        self.assertEqual(solucion, "No es un mensaje")

    def test_palabras_repetidas(self):
        """
            Testea usar más de una vez la misma palabra del diccionario.
        """
        cadena = "cadenadepruebacadenatp2"
        palabras = {"prueba", "tp2", "cadena", "el", "para", "de"}
        solucion = validar_mensaje(cadena, palabras)
        self.assertIsNotNone(solucion)
        self.assertEqual(len(solucion.split()), 5)

    def test_cadena_una_palabra(self):
        """
            Testea con una cadena de una sola palabra.
        """
        cadena = "cadena"
        palabras = {"prueba", "tp2", "cadena", "el", "para", "de"}
        solucion = validar_mensaje(cadena, palabras)
        self.assertIsNotNone(solucion)
        self.assertGreater(len(solucion), 1)

    def test_cadena_vacia(self):
        """
            Testea con una cadena vacía.
        """
        cadena = ""
        palabras = {"prueba", "tp2", "cadena", "el", "para", "de"}
        solucion = validar_mensaje(cadena, palabras)
        self.assertIsNotNone(solucion)
        self.assertEqual(len(solucion), 0)

    def test_diccionario_amplio(self):
        """
            Testea una única cadena con un diccionario de palabras largas.
        """
        cadena = "xnvlitwgkkbxgqjafioutmdtcsmerbdsigytrkpyiocwetxpdmwexpqhzqxpnkczdquneqkzeatpypgczdgnhbvrcgrzlwrxgmuevmnucorcjwxgxogegtyfzleitiyrgzfrfbtgdoqbekraqdbpcgneqcguxhghfzrkeskamvozbtuxsetqorswmyzkewxgnmdgipjdqdlzbfzsqdcroloynqtkhwsbhbvhblbwvvdlzmpqohraqolzdxsetkmothzpwdvdbtgqlrpfgfyviwghnivduazjtdjbbbhuakvapaonuoeranmuxbgyjnwwfiyiehwkhrwhmdvhippxdttfpbiwlsqjrutpgollhnopfynqeepyvymolojrgjryxtjaifdqzotbizxuzhfplauycjdzttjyybqbejqcvtiyxcavbgeybbhvwwkcpunldp"
        palabras = procesar_archivo("Archivos/palabras_con_mucha_amplitud.txt")
        solucion = validar_mensaje(cadena, palabras)
        self.assertIsNotNone(solucion)
        self.assertGreater(len(solucion), 1)

    def test_diccionario_mayor_a_cadena(self):
        """
            Test con diccionario mayor a largo cúbico de cadena.
        """
        cadena = "sywmmwxbvfpkswbrxzhhilyspnhgowynutkyqnollogna"
        palabras = procesar_archivo("Archivos/750milpalabras.txt")
        solucion = validar_mensaje(cadena, palabras)
        self.assertIsNotNone(solucion)
        self.assertGreater(len(solucion), 1)

    def test_cadena_largo_10000(self):
        """
            Test con cadena de largo 10000.
        """
        cadena = "migredesvencijamosasicaraisestriñesedespojaserelenteceríaschonguearíamosdesahijaréempalizarasincursionaréisreencaucháisfanaticadaniveladorremolieseisreimpresiónmangabaisahuesásimpartirédistrajéremoszonificaríaninternaseislindaronaricasteischepeadepítimamonumentalizaránlicenciosamenteafollabaispremeditamosenhebrarégestaranescullésemoscargadillarevoleabanensoparíaescamotearesprioralcombustionareisjaloneabasdesenvolverácerebralpastencotransmutarenfrenetizófrenetizóchantaríamosajizasteislastimaronsobrealimentarasrecetemosdemacrasesobrescribiéremosprocesionaréisincursionaréisengastadoraagarrásemosemplomesenrambladescullésemosdevalebandericesbolseáremosenhechizábamosroblonaréiscorcovaríaistransmutarenentamarárefrenasecaceroleenencapucesboludeadremansenestriñesehumectaréisescalabraríadesfondabanniveladorcreceránamenorabaiscintaresmerquemosredoblegasesacuantiadoarrequesonaranserruchásemoscastañeesresistiesescontlapacheabaisrepisasesbardáscontlapacheabaisvitivinicultoraabanderastedestruirácorporificarbrujeamosdesposabaspredefináisdesasociandoremansenestirabarepisasesapoquinadocontendierasentrecogeríasteatralizabaisruñareisempobrezcáislicenciosamentearrocinenovillarásenconaremosencureñáiscargadillaentamarácagüeñoballesteoteatralizabaisvenencienfeudáremosdesguabilaríancaciqueanremostéfintaroncopucháremosencanalaríaemparraréistaráremosincitabaludificaseiscachifollareisrefrenaseruleteasecausticarenescullésemossendereartexturicemosentrecomemosarabigoandaluzapredefináisrecomieseprogresióntempesteasescroajarasapropincuasencerebralencallezcanfestoneandocuabacustodiabasvociferaransobajaránencaletáremosrosqueaseisrompéisexpeditáramosdistrajéremosdeslatarensobresanabanresquemabaslandócinchaceábamosespatarraranraramentecontemplemosinvernandistenderíasdesbullarenentrecomemossuplieroncontemplemosestofarenacrezcoenrasasretinasestiraflechascaciqueanpreordinarérepapilabaisbotoneenmallasempellíamoslindaroncolapsandocircunvolamosopíparodesbarrigaríasacomodarasmaltearanzonificaríanbarulleracolimásolicitasteisbunkericepapujocitoesqueletocopeteabadescongestionoatolondraressaludarásrehoguébostezadoraquinchabareexpedisteiscontraloreemosexpeledconllevaráredondeasteaturrullancastañeesbadulaqueemosarranchadotaráremoscoquearaenviésarpullíamosamustiarásamantillaréisenrambladfestoneandolosinapirgüínbailasedesendemonienumerarashorripilesabullonéisjaloneabasbailaseagenciaronbotoneenmoldandoversionamospreordinarépachorreestimadoradescalabresarpullíamoscombustionareisquilatarérepisasesemparraréisdesempercudiríantraqueteomoldandotutorizaraisalambicaríaisdesnarigaráensoparíadestallaríaisamollentesfeudareisidealidadventemosexpeditáramosgarberapreordinarélastimaronlenificarescontrolaráempajarresentírefundodesaparecíaspañosoagobiarásidealidadcondecoranpremeditamosexpoliadvenencienbostezadorahomogeneicesligustrinapalabrearéisbajáremostornavozempalagabaisdeslatarendesembridareisremolieseislincearándesportillemosencenizaríassolidaridadjingláfanfarronearemosdesamuraríanrecamaremoscríenquinchabaretronandodesalbardarásloreadoconlleváramosuparéendiosesmercadeadcuqueábamosdebutesencanalaríaofendieronretronandodesportillemosdesinficionarancanibalizaríaisderogáisredhibísdesatentemosafielaríamosepitimaronagarrásemosdesunidapoquinasenencubaríaisametrallójicarudapreferirásmensualmenterugíanalemandaalmacenemosinjurianteinventáremosvalladearíapichelingueamilagrándocoaccionéisdesenvolverásesionéimpacientementeasaderapesquisastequitabasdesempulgasenreptábamosapropincuasendesrabotarábaladraremosacocearmofemosmunicionaredescarrilehorquillabapreternaturalizapejesapoponcharonmilongueoalejamosmallascuarteléisaceitaascorosorebrametasadsilenciandosarpullíamoszambutiesesincitabaarrocinenencabulléisdesnarigaráesguardeovillarásencauchadaenvidiaríacombaríasobrevendieranindisciplíneagarrásemosracimaronacrezcopiafabasdesempulgasenrecaudemosmartajarenburbujearíaachulapabaencintarasaderafajinearíasparlamentaronaviábamosenramáisboicoteemosentretuviesesenconaremosabreviaratornavoznumerarasacomedísbrisquefeudareisrelatabaismotilasamustiarásderrenegaráinvernandesgarrabaiscarcomiórecaudemosrecamaremospungiríaisdeseduquemospanceraarruáramossabatizabaisobranucrónicaefetáderogáisrenieguenentabacarenalejamosdestriunfadoadorareisentrecomemosrecaudemoslebranchocuabadesimaginásemosabúlicachepeemosencajeráramosascorosoepítimadesgargolaremitigativorochabaiscreceránaceitaencorreabasachuráisflectamosrasparéerupcionaríanencorreabasencanillódesestabilizarancoquearaeuropeizábamosexcogitareisabracaríanpisparíaresacáentrepeladraciocinaraisucrónicadescalificáiscerebraldesbalagarásdeseasenprefigurancarnadadecorticanozonizasenvalladearíatiranizopolaquearíasrefrenasecuqueábamosludificaseiscausticarencomplejizatalegadasamnitefulgurasencontagiaríaismentarasalojaréinternaseistravesaraispositivesesperanzaseisconfeccionabaisaseveraseissamnitefusionamosquitabasdesentorpeceríafusionamoscolapsandoobranexpeléisdesposabasventemosacezastementarasburbujearíaludificaseisoprobiaréisrecetaremoshorquillabaencañonóvitivinicultorarecetaremosajizaraflagelolindaronescamotearessanjuanearehacenderaprefiguranevanesciesecuabausesfintaronadjetivaraestimasasombraranendiosesdesvenadmunicionareempellíamosdesempegaddesincrustáremosgelaresdescalificáisretinasesregocijaráobranorientalizarádestruirácarnadacumbeédesenvendesametrallódeshebillabasdescorreareamoralidadsubemplearíaamilagrándohostajedesvitalicemoschiclearasmalviviríaninventariásfisgadordesapadrinaréiscabrahigaraisempurarenempalagabaisfolletinistadesemballestaenhorquetabaisloleecachifollareisexcogitaríandesistieseencintarencanillóchonguearíamoszapeanadjetivaradesincrustabaissobrealimentarasbeneficiemosescalabraríabalareisaculatareisengastadoraentamaráchiclearasdesguabilaríanprotagónicaenviéonceenpreferirásentrecomemosepitimaronexpeleddantadireccionocargadillazambutiesesquitaraispontificiodesahijarégesticulécontingentarancontemplemosdeseasenchollábarulleradistenderíasmercadeadfranquiciabasponcharondesahijarédemasiaseengrampábamosinsuflásrestriñaspositivesdesistieseentabanesacezasteprotocoléfolletinistaempapirotéisparlamentaronencenderemosvalladearíacomplejizamayordomeásemplomarafosaartocárpeocoaccionéisfiestaremosdesenfurruñasubarrendarescroajarasdotatrafagastedesenfurruñaapomazáscaceroleenreproducciónespatarraranreproduccióndesenfaldesencantorioatildaseisdeleznesescamotearespululaseguayabearenescamotearescomisendespancijamosdesapolillásemosquitabasescullésemosparrizacondecorandesparedabaevertiesemancipasteisarponearaencenizaríasecologicéabúlicaresanáremosdesincentivaríapasajelebranchodesortijenadarvaríasabatizabaisempapirotéisapandábamoscaceroleencuabamaestrejicomarinjurianteenfielasteemparraréisemplomesbarulleracomprometíanchamboneadalmagreñaestucadcastañeescopeteabaregenciasubemplearíaranclesafielaríamoslosinaelegantizasecacháremospuniríanrecomiesedesciñendocontornearéencenizaríasefetáitalianizandoteledirigióherbolaranasicaraisempurarencanecerásquilataréacomedísadquiereredondeasteencabulléisajardinaríaescarnecídesvitalicemosadjetivaragargajeandodesenchilabadeleznescoquearapachorreesrevoleabanenvinabalicenciosamentepisparíaestimasrenieguenpichelinguecombaríadeshebillabasamanzanenbalareiscaribearesfajinearíasbasculabanagrínguendesesterásenramáisentramparaviajásemosecologicéitalianizandoperifoneaseisflageloperifoneaseisagarrásemosenlardéisengastadorabalareisrepapilabaisafosadesbocareisescarnecídesaparecíascopeteabacambiotenafisgadorcontornearédespojasenegociableengranemosespatarraranbunkericegarberasuplieronaheleábamossarpullíamosdesintoxicaríadesfondabanregenciamitigativoarrocinencolimáalejamosváucherdesfolloneamurcabacagüeñogargajeandodespachanreprendéloreadoparamentarenconlleváramosajocháramosprefigurantemplaredesamuraríanrepueblamercuriosoevertiesecachifollareistrastornaríamosemparraréismedrecontornearéhincásemosdesahijarédesamortizaríaamanzanenincitabaescarzasarrasarandemasiasecuarteléisdespachanpasterizaríaestofarendesenfaldesaperdigaresenchibolédescachimbarancundieresevertiesedesliñeexcogitaríanprosperaarrutinasevitivinicultoraholgazanearédesapolillásemosfanfarronearemosaviábamospejesapoescamocharemosdesistiesecarocharenconlleváramostitularizaríanamuinéguayabearenfinaseninjuriábamosdescarrilemanjolareesguarderefringirtamboreterochabaislambiscabaquitaraisestarcíschiripearaspontificiorefrenaserepisasesinventáremoscontraloreemosvalidandoobranatabaleasabullonéishomogeneicesabejearíasrebombarépesquisastedesemballestadestachonáremostriplicarápostinearíamosrecetemosdesapolillásemosaheleábamospíefusionamosabracemosaperdigaresaseveraseiscontritaencenderemosenfangaseisdesembrazarloreadoquitabaslenificaresderretimosdesfondabanguayabearenincomuniquemosraizarencontorsionofirmáramostizoneareisalheñaríarestriñasachuráisarruinamientoestarcísaseriéderogáisbrisquefulgurasenconstatáisarrocinenembarriaríasborraríasbestializamosabejearíasdesciñendoabanderastecuabademasiasearruáramosendiosesrefringirempobrezcáisdestachonáremosmilongueotriplicarápungiríaisauscultaranencenizaríastriplicarámigrecontraargumentaroncuqueábamossimonizaraantecedisteisdestriunfadoanisómeracalendadlopescotasaddesempegaddescordasteacezasteaturrullancédulaentuturutáremosteatralizabaispancutradesaforrasescomisenagrínguencumbeélistelbingueraagrínguenucrónicaarrequesonarandesposabascinchaceábamostraqueteofeudareiscohechasteisludificaseisquebraraisboludeadembarriaríasensoparíaencangrejaremosapoquinasenrecalcáramosaniquilaremoscagüeñosubsidiasemonumentalizaránafocarestamboretecacalotedesimaginásemosenchiboléclareandosocapóreprendéhorquillabaatroneraríanresquemabasballesteodesenverguesdecenteshorripilesembruteciéremostalidaddesembrazarreblandecierenantecedisteisfestoneandobunkericefusilabapachorreeszapeanrelatabaisonceenrebombarésaludarásesperanzaseispañososolicitasteissubemplearíadespavonarescombustionareiscontagiaríaisenancabaisdesencantotejarozgoteabaiscoquearavociferaranchusmeaempamparíaislambiscabamaltearanamurcabagargajeandodesliñemayordomeásdevalemonumentalizaránespesenoprobiaréismalvezaríaisindisciplíneprotagónicaguantanamera"
        palabras = procesar_archivo("Archivos/supergigante.txt")
        solucion = validar_mensaje(cadena, palabras)
        self.assertIsNotNone(solucion)
        self.assertGreater(len(solucion), 1)

    def test_10_corto(self):
        """
            Test de volumen 10 con diccionario corto.
        """
        self.ejecutar_test("Archivos/10_in.txt", "Archivos/corto.txt", {1, 2, 6, 12})

    def test_50_corto(self):
        """
            Test de volumen 50 con diccionario corto.
        """
        self.ejecutar_test("Archivos/50_in.txt", "Archivos/corto.txt", {2, 8, 9})

    def test_120_corto(self):
        """
            Test de volumen 120 con diccionario corto.
        """
        self.ejecutar_test("Archivos/120_in.txt", "Archivos/corto.txt", {0, 6})

    def test_15_mediano(self):
        """
            Test de volumen 15 con diccionario mediano.
        """
        self.ejecutar_test("Archivos/15_in.txt", "Archivos/mediano.txt", {2, 3, 4, 5, 14})

    def test_70_mediano(self):
        """
            Test de volumen 70 con diccionario mediano.
        """
        self.ejecutar_test("Archivos/70_in.txt", "Archivos/mediano.txt", {0, 2, 3, 5, 8, 11, 13, 16, 19})

    def test_100_mediano(self):
        """
            Test de volumen 100 con diccionario mediano.
        """
        self.ejecutar_test("Archivos/100_in.txt", "Archivos/mediano.txt", {2})

    def test_60_grande(self):
        """
            Test de volumen 60 con diccionario grande.
        """
        self.ejecutar_test("Archivos/60_in.txt", "Archivos/grande.txt")

    def test_80_grande(self):
        """
            Test de volumen 80 con diccionario grande.
        """
        self.ejecutar_test("Archivos/80_in.txt", "Archivos/grande.txt", {9})

    def test_150_grande(self):
        """
            Test de volumen 150 con diccionario grande.
        """
        self.ejecutar_test("Archivos/150_in.txt", "Archivos/grande.txt", {3})

    def test_200_gigante(self):
        """
            Test de volumen 200 con diccionario gigante.
        """
        self.ejecutar_test("Archivos/200_in.txt", "Archivos/gigante.txt")

    def test_500_gigante(self):
        """
            Test de volumen 500 con diccionario gigante.
        """
        self.ejecutar_test("Archivos/500_in.txt", "Archivos/gigante.txt", {4})

    def test_2000_supergigante(self):
        """
            Test de volumen 2000 con diccionario supergigante.
        """
        self.ejecutar_test("Archivos/2000_in.txt", "Archivos/supergigante.txt", {2})

    def test_5000_supergigante(self):
        """
            Test de volumen 5000 con diccionario supergigante.
        """
        self.ejecutar_test("Archivos/5000_in.txt", "Archivos/supergigante.txt", {0, 3})

    def test_5000_supergigante_y_optimizacion(self):
        """
            Test de volumen 5000 con diccionario supergigante y optimizacion de largos mín/máx.
        """
        cadenas = procesar_archivo("Archivos/5000_in.txt")
        palabras = set(procesar_archivo("Archivos/supergigante.txt"))
        largo_min, largo_max = buscar_largos_extremos(palabras)
        
        for i in range(len(cadenas)):
            solucion = validar_mensaje(cadenas[i], palabras, largo_min, largo_max)
            if i in {0, 3}:
                self.assertEqual(solucion, MSG_NO_ES)
            else:
                self.assertNotEqual(solucion, MSG_NO_ES)
                self.assertGreater(len(solucion), 1)

    def test_lorem_ipsum(self):
        """
            Test de volumen 10 con diccionario de latín.
        """
        self.ejecutar_test("Archivos/lorem_ipsum_in.txt", "Archivos/lorem_ipsum_words.txt")

class ColoredTestResult(unittest.TextTestResult):
    def getDescription(self, test):
        doc = test.shortDescription()
        if doc:
            return doc
        return str(test)
    
    def addSuccess(self, test):
        super().addSuccess(test)
        self.stream.writeln(f"{self.getDescription(test)} - {GREEN}OK{RESET}")

    def addFailure(self, test, err):
        super().addFailure(test, err)
        self.stream.writeln(f"{self.getDescription(test)} - {RED}ERROR{RESET}")

class ColoredTestRunner(unittest.TextTestRunner):
    resultclass = ColoredTestResult

def main():
    # Usa nuestro runner coloreado
    runner = ColoredTestRunner(verbosity=1)
    unittest.main(testRunner=runner, exit=False)

if __name__ == "__main__":
    main()