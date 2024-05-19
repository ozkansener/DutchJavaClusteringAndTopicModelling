import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class StoryClustering {
    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
            "en", "een", "de", "hij", "het", "aan", "aangaande", "aangezien", "achte", "achter", "achterna",
            "af", "afgelopen", "al", "aldaar", "aldus", "alhoewel", "alias", "alle", "allebei", "alleen", "alles",
            "als", "alsnog", "altijd", "altoos", "ander", "andere", "anders", "anderszins", "beetje", "behalve",
            "behoudens", "beide", "beiden", "ben", "beneden", "bent", "bepaald", "betreffende", "bij", "bijna",
            "bijv", "binnen", "binnenin", "blijkbaar", "blijken", "boven", "bovenal", "bovendien", "bovengenoemd",
            "bovenstaand", "bovenvermeld", "buiten", "bv", "daar", "daardoor", "daarheen", "daarin", "daarna",
            "daarnet", "daarom", "daarop", "daaruit", "daarvanlangs", "dan", "dat", "de", "deden", "deed", "der",
            "derde", "derhalve", "dertig", "deze", "dhr", "die", "dikwijls", "dit", "doch", "doe", "doen", "doet",
            "door", "doorgaand", "drie", "duizend", "dus", "echter", "een", "eens", "eer", "eerdat", "eerder",
            "eerlang", "eerst", "eerste", "eigen", "eigenlijk", "elk", "elke", "en", "enig", "enige", "enigszins",
            "enkel", "er", "erdoor", "erg", "ergens", "etc", "etcetera", "even", "eveneens", "evenwel", "gauw",
            "ge", "gedurende", "geen", "gehad", "gekund", "geleden", "gelijk", "gemoeten", "gemogen", "genoeg",
            "geweest", "gewoon", "gewoonweg", "haar", "haarzelf", "had", "hadden", "hare", "heb", "hebben", "hebt",
            "hedden", "heeft", "heel", "hem", "hemzelf", "hen", "het", "hetzelfde", "hier", "hierbeneden",
            "hierboven", "hierin", "hierna", "hierom", "hij", "hijzelf", "hoe", "hoewel", "honderd", "hun", "hunne",
            "ieder", "iedere", "iedereen", "iemand", "iets", "ik", "ikzelf", "in", "inderdaad", "inmiddels",
            "intussen", "inzake", "is", "ja", "je", "jezelf", "jij", "jijzelf", "jou", "jouw", "jouwe", "juist",
            "jullie", "kan", "klaar", "kon", "konden", "krachtens", "kun", "kunnen", "kunt", "laatst", "later",
            "liever", "lijken", "lijkt", "maak", "maakt", "maakte", "maakten", "maar", "mag", "maken", "me",
            "meer", "meest", "meestal", "men", "met", "mevr", "mezelf", "mij", "mijn", "mijnent", "mijner",
            "mijzelf", "minder", "miss", "misschien", "missen", "mits", "mocht", "mochten", "moest", "moesten",
            "moet", "moeten", "mogen", "mr", "mrs", "mw", "na", "naar", "nadat", "nam", "namelijk", "nee", "neem",
            "negen", "nemen", "nergens", "net", "niemand", "niet", "niets", "niks", "noch", "nochtans", "nog",
            "nogal", "nooit", "nu", "nv", "of", "ofschoon", "om", "omdat", "omhoog", "omlaag", "omstreeks",
            "omtrent", "omver", "ondanks", "onder", "ondertussen", "ongeveer", "ons", "onszelf", "onze", "onzeker",
            "ooit", "ook", "op", "opnieuw", "opzij", "over", "overal", "overeind", "overige", "overigens", "paar",
            "pas", "per", "precies", "recent", "redelijk", "reeds", "rond", "rondom", "samen", "sedert", "sinds",
            "sindsdien", "slechts", "sommige", "spoedig", "steeds", "tamelijk", "te", "tegen", "tegenover", "tenzij",
            "terwijl", "thans", "tien", "tiende", "tijdens", "tja", "toch", "toe", "toen", "toenmaals", "toenmalig",
            "tot", "totdat", "tussen", "twee", "tweede", "u", "uit", "uitgezonderd", "uw", "vaak", "vaakwat", "van",
            "vanaf", "vandaan", "vanuit", "vanwege", "veel", "veeleer", "veertig", "verder", "verscheidene",
            "verschillende", "vervolgens", "via", "vier", "vierde", "vijf", "vijfde", "vijftig", "vol", "volgend",
            "volgens", "voor", "vooraf", "vooral", "vooralsnog", "voorbij", "voordat", "voordezen", "voordien",
            "voorheen", "voorop", "voorts", "vooruit", "vrij", "vroeg", "waar", "waarom", "waarschijnlijk", "wanneer",
            "want", "waren", "was", "wat", "we", "wederom", "weer", "weg", "wegens", "weinig", "wel", "weldra",
            "welk", "welke", "werd", "werden", "werder", "wezen", "whatever", "wie", "wiens", "wier", "wij",
            "wijzelf", "wil", "wilden", "willen", "word", "worden", "wordt", "zal", "ze", "zei", "zeker", "zelf",
            "zelfde", "zelfs", "zes", "zeven", "zich", "zichzelf", "zij", "zijn", "zijne", "zijzelf", "zo", "zoals",
            "zodat", "zodra", "zonder", "zou", "zouden", "zowat", "zulk", "zulke", "zullen", "zult"
    ));

    public static void main(String[] args) {
        List<String> stories = generateStories();

        // Preprocess the stories
        List<String> cleanedStories = stories.stream().map(StoryClustering::cleanText).toList();

        // Create TF-IDF vectors
        double[][] tfidfVectors = createTFIDFVectors(cleanedStories);

        // Calculate similarity matrix
        double[][] similarityMatrix = calculateSimilarityMatrix(tfidfVectors);

        // Print similarity matrix
        printMatrix(similarityMatrix);

        // Cluster the stories into 4 categories using KMeans
        List<List<String>> clusters = clusterStoriesWithKMeans(tfidfVectors, cleanedStories, 4);

        // Generate cluster names using the top terms in each cluster
        List<String> clusterNames = generateClusterNames(clusters);

        // Print clusters with names
        printClusters(clusters, clusterNames);

    }

    private static String cleanText(String text) {
        String cleanedText = text.replaceAll("[^a-zA-Z0-9\\s]", "").toLowerCase();
        return Arrays.stream(cleanedText.split("\\s+"))
                .filter(word -> !STOPWORDS.contains(word))
                .collect(Collectors.joining(" "));
    }

    private static double[][] createTFIDFVectors(List<String> stories) {
        Directory directory = new ByteBuffersDirectory();
        Analyzer analyzer = new StandardAnalyzer(new CharArraySet(STOPWORDS, true));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        try (IndexWriter writer = new IndexWriter(directory, config)) {
            for (String story : stories) {
                Document doc = new Document();
                doc.add(new TextField("content", story, Field.Store.YES));
                writer.addDocument(doc);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        double[][] tfidfVectors = new double[stories.size()][];

        try (DirectoryReader reader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            searcher.setSimilarity(new BM25Similarity());

            for (int i = 0; i < stories.size(); i++) {
                // Initialize tfidfVectors with zero length
                tfidfVectors[i] = new double[stories.size()];
                String[] words = stories.get(i).split("\\s+");

                for (String word : words) {
                    Term term = new Term("content", word);
                    TermQuery query = new TermQuery(term);
                    TopDocs topDocs = searcher.search(query, 1);
                    if (topDocs.totalHits.value > 0) {
                        int docId = topDocs.scoreDocs[0].doc;
                        tfidfVectors[i][docId] += searcher.explain(query, docId).getValue().doubleValue();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tfidfVectors;
    }

    private static double[][] calculateSimilarityMatrix(double[][] tfidfVectors) {
        int n = tfidfVectors.length;
        double[][] matrix = new double[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                double similarity = calculateSimilarity(tfidfVectors[i], tfidfVectors[j]);
                matrix[i][j] = similarity;
                matrix[j][i] = similarity;
            }
        }

        return matrix;
    }

    private static void printMatrix(double[][] matrix) {
        for (double[] row : matrix) {
            for (double value : row) {
                System.out.printf("%.2f ", value);
            }
            System.out.println();
        }
    }

    private static List<List<String>> clusterStoriesWithKMeans(double[][] tfidfVectors, List<String> stories, int k) {
        // Perform KMeans clustering with similarity threshold
        List<Set<Integer>> clusters = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            clusters.add(new HashSet<>());
        }

        boolean[] assigned = new boolean[stories.size()];
        int clusterIndex = 0;

        for (int i = 0; i < tfidfVectors.length; i++) {
            if (assigned[i]) continue;
            clusters.get(clusterIndex).add(i);
            assigned[i] = true;
            for (int j = 0; j < tfidfVectors.length; j++) {
                if (!assigned[j] && calculateSimilarity(tfidfVectors[i], tfidfVectors[j]) > 0.9) {
                    clusters.get(clusterIndex).add(j);
                    assigned[j] = true;
                }
            }
            clusterIndex = (clusterIndex + 1) % k;
        }

        return clusters.stream()
                .map(cluster -> cluster.stream()
                        .map(stories::get)
                        .toList())
                .toList();
    }

    private static double calculateSimilarity(double[] vector1, double[] vector2) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            normA += Math.pow(vector1[i], 2);
            normB += Math.pow(vector2[i], 2);
        }

        // Prevent division by zero by ensuring norms are non-zero
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }


    private static List<String> generateClusterNames(List<List<String>> clusters) {
        List<String> clusterNames = new ArrayList<>();
        for (List<String> cluster : clusters) {
            String combinedText = String.join(" ", cluster);
            String[] words = combinedText.split("\\s+");
            Map<String, Integer> wordCounts = new HashMap<>();
            for (String word : words) {
                wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
            }
            List<Map.Entry<String, Integer>> sortedWords = wordCounts.entrySet()
                    .stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(5)
                    .toList();
            String clusterName = sortedWords.stream()
                    .map(Map.Entry::getKey)
                    .collect(Collectors.joining(", "));
            clusterNames.add(clusterName);
        }
        return clusterNames;
    }

    private static void printClusters(List<List<String>> clusters, List<String> clusterNames) {
        for (int i = 0; i < clusters.size(); i++) {
            System.out.println("Cluster " + (i + 1) + " (" + clusterNames.get(i) + "):");
            for (String story : clusters.get(i)) {
                System.out.println(" - " + story);
            }
        }
    }

    private static List<String> generateStories() {
        List<String> stories = new ArrayList<>();
        // Verhaal 1: De moedige ridder
        stories.add("Er was eens een dappere ridder genaamd Lancelot. Hij woonde in een groot kasteel met zijn koning en koningin. Op een dag hoorde Lancelot dat er een draak een naburig dorp terroriseerde. Zonder aarzelen vertrok hij op zijn stalen ros om het dorp te redden. Na een lange en gevaarlijke reis arriveerde Lancelot in het dorp. De draak was enorm en spuwde vuur uit zijn neusgaten. Maar Lancelot was niet bang. Hij vocht dapper tegen de draak en versloeg hem uiteindelijk. De dorpelingen juichten en vierden Lancelot als hun held.");

        // Verhaal 2: De slimme prinses
        stories.add("In een ver koninkrijk woonde er een prinses genaamd Isabella. Ze was niet alleen mooi, maar ook erg intelligent. Op een dag organiseerde de koning een toernooi om een geschikte echtgenoot voor zijn dochter te vinden. Alle ridders uit het land kwamen naar het toernooi om te vechten om de hand van Isabella. Maar geen van hen kon haar intellectuele uitdagingen oplossen. Totdat er op een dag een onbekende ridder verscheen. Hij was niet de sterkste of mooiste ridder, maar hij was wel de slimste. Hij beantwoordde al Isabella's vragen met gemak en won haar hart. De koning was blij met de keuze van zijn dochter en ze leefden nog lang en gelukkig.");

        // Verhaal 3: De magische tovenaar
        stories.add("In een oud bos woonde er een machtige tovenaar genaamd Merlin. Hij had eeuwenlang geleefd en kende alle geheimen van magie. Op een dag kwam er een jonge vrouw naar Merlin toe. Ze was verdrietig omdat haar geliefde was vervloekt door een boze heks. Merlin had medelijden met de vrouw en besloot haar te helpen. Hij gebruikte zijn magie om de vloek te verbreken en het stel leefde nog lang en gelukkig.");

        // Verhaal 4: De verloren schat
        stories.add("Een groep avonturiers ging op zoek naar een verloren schat die ergens in een verlaten jungle was verborgen. Ze reisden wekenlang door dichte bossen, over woeste rivieren en langs gevaarlijke kliffen. Onderweg moesten ze wilde dieren bevechten, vallen ontwijken en raadsels oplossen. Na veel tegenslag vonden ze eindelijk de schat. Ze waren rijk en gelukkig, maar misten hun avontuurlijke leven.");

        // Verhaal 5: De verboden liefde
        stories.add("Romeo en Juliet waren twee jonge mensen uit rivaliserende families. Ze werden verliefd ondanks de haat tussen hun families. Ze trouwden in het geheim, maar hun geluk was van korte duur. Romeo werd verbannen en Juliet gedwongen om met een andere man te trouwen. Op de dag van haar bruiloft dronk Juliet een drankje dat haar in een diepe slaap bracht. Toen Romeo terugkwam, dacht hij dat ze dood was en pleegde hij zelfmoord. Toen Juliet wakker werd, vond ze Romeo dood en doodde ze zichzelf met zijn dolk.");

        // Verhaal 6: De dappere boer
        stories.add("In een klein dorp woonde er een boer genaamd Hans. Op een dag viel er een draak aan op het dorp. De ridders waren bang en vluchtten, maar Hans was vastberaden om zijn familie en buren te beschermen. Hij nam een oud zwaard dat hij op zolder had gevonden en ging de draak tegemoet. Met veel moed en doorzettingsvermogen wist hij de draak te verslaan en het dorp te redden. Hans werd een held en het dorp leefde voortaan in vrede.");

        // Verhaal 7: De wijze oude man
        stories.add("In een klein dorpje woonde er een oude man die bekend stond om zijn wijsheid. Mensen uit het hele land kwamen naar hem toe voor advies. Op een dag kwam er een jonge prins naar hem toe, zoekend naar antwoorden op levensvragen. De oude man vertelde hem verhalen en leerde hem over de waarde van geduld, vriendelijkheid en moed. De prins keerde terug naar zijn koninkrijk als een veranderde man en regeerde rechtvaardig en wijs, geliefd door zijn volk.");

        // Verhaal 8: De geheimzinnige grot
        stories.add("Op een dag ontdekte een groep kinderen een geheime grot in het bos. Ze besloten naar binnen te gaan en ontdekten een wereld vol magische wezens en verborgen schatten. Ze beleefden talloze avonturen en maakten nieuwe vrienden. Uiteindelijk keerden ze terug naar hun dorp, maar de herinneringen aan de magische wereld en de lessen die ze daar leerden, bleven voor altijd bij hen.");

        // Verhaal 9: Het betoverde bos
        stories.add("Er was eens een betoverd bos waar de bomen konden spreken en de dieren konden zingen. Een jonge jager verdwaalde op een dag in het bos en ontmoette de magische bewoners. Hij ontdekte dat het bos vervloekt was door een kwade heks. Samen met de dieren en de bomen begon hij aan een gevaarlijke zoektocht om de vloek te verbreken. Na veel avonturen en gevaren slaagde hij erin om de heks te verslaan en het bos te bevrijden. De jager werd een held en het bos bloeide weer op.");

        // Verhaal 10: De verdwenen prins
        stories.add("In een groot koninkrijk werd de prins op een dag ontvoerd door vijandige soldaten. De koning en koningin waren ontroostbaar en het hele land rouwde om de prins. Een dappere ridder nam het op zich om de prins te redden. Hij reisde door vijandige gebieden, vocht tegen soldaten en doorstond vele beproevingen. Uiteindelijk vond hij de prins en bracht hem veilig terug naar het paleis. De koning en koningin waren dolgelukkig en de ridder werd beloond met grote eer en rijkdom.");

        // Verhaal 11: De slimme dief
        stories.add("Er was eens een slimme dief genaamd Robin Hood die in het bos van Sherwood woonde. Hij stal van de rijken en gaf aan de armen. Zijn vijand, de sheriff van Nottingham, probeerde hem keer op keer te vangen, maar Robin Hood was altijd een stap voor. Met zijn slimme trucs en behulp van zijn trouwe vrienden wist hij de sheriff telkens weer te slim af te zijn. Robin Hood werd een legende en zijn verhalen werden doorgegeven van generatie op generatie.");

        // Verhaal 12: Het verlaten kasteel
        stories.add("Op een heuvel stond een oud en verlaten kasteel dat al eeuwenlang niet meer bewoond was. De mensen uit het nabijgelegen dorp vertelden verhalen over spoken en verborgen schatten in het kasteel. Op een dag besloot een moedige jongeman om het kasteel te verkennen. Hij ontdekte geheime gangen, oude relikwieën en inderdaad enkele geesten die in vrede wilden rusten. Door zijn dapperheid en respect wist hij de geesten te bevrijden en vond hij een verborgen schat. Het kasteel werd hersteld en werd een symbool van hoop voor het dorp.");

        // Verhaal 13: De magische spiegel
        stories.add("Er was eens een magische spiegel die de toekomst kon voorspellen. Hij werd bewaard door een oude heks die alleen de dapperste en meest waardige mensen toestond om hem te gebruiken. Een jonge vrouw die op zoek was naar haar lot vond de spiegel en de heks. Na het doorstaan van verschillende tests mocht ze de spiegel gebruiken. Ze zag haar toekomst en leerde belangrijke lessen over liefde, moed en doorzettingsvermogen. Met deze kennis keerde ze terug naar haar dorp en veranderde haar leven ten goede.");

        // Verhaal 14: De reus en de kleermaker
        stories.add("Er was eens een kleine kleermaker die bekend stond om zijn moed en slimheid. Op een dag hoorde hij dat er een reus het dorp terroriseerde. De kleermaker besloot om de reus te confronteren. Hij gebruikte zijn verstand en creativiteit om de reus te slim af te zijn. Uiteindelijk wist hij de reus te vangen en het dorp te redden. De dorpelingen vierden de kleermaker als hun held en hij werd beroemd om zijn dapperheid en intelligentie.");

        // Verhaal 15: Het geheim van de oude bibliotheek
        stories.add("In een oude bibliotheek die op de rand van een dorp stond, waren er geruchten over een verborgen kamer vol met oude en magische boeken. Een jonge leerling die dol was op lezen besloot de bibliotheek te verkennen. Na weken van zoeken en lezen ontdekte hij eindelijk de verborgen kamer. Hij vond boeken vol met vergeten kennis en magie. Met deze kennis hielp hij zijn dorp om problemen op te lossen en verbeterde hij het leven van zijn dorpsgenoten.");

        // Verhaal 16: De betoverde tuin
        stories.add("Er was eens een prachtige tuin die door een oude tovenaar was betoverd. De bloemen daar bloeiden het hele jaar door en de vruchten waren altijd zoet. Maar niemand durfde de tuin binnen te gaan omdat er geruchten gingen dat hij vervloekt was. Op een dag besloot een dappere jongeman de tuin te verkennen. Hij ontdekte dat de tuin niet vervloekt was, maar beschermd werd door magische wezens. Hij maakte vrienden met de wezens en leerde over de magie van de tuin. Hij bracht deze kennis terug naar zijn dorp en hielp zijn dorpsgenoten om hun eigen tuinen te verbeteren.");

        // Verhaal 17: De ridder en de draak
        stories.add("Er was eens een ridder die bekend stond om zijn moed en kracht. Op een dag hoorde hij dat er een draak het land terroriseerde. De ridder trok zijn wapenrusting aan en ging op zoek naar de draak. Na dagen van zoeken vond hij de draak in een donkere grot. Hij vocht een hevig gevecht en wist de draak uiteindelijk te verslaan. De mensen in het land waren dolgelukkig en de ridder werd geprezen als een grote held. Hij keerde terug naar zijn kasteel en leefde nog lang en gelukkig.");

        // Verhaal 18: Het mysterieuze eiland
        stories.add("Op een dag ontdekte een groep zeevaarders een mysterieus eiland dat niet op hun kaarten stond. Ze besloten het eiland te verkennen en vonden een wereld vol vreemde wezens en onbekende planten. Ze ontdekten dat het eiland door een oude tovenaar was betoverd en dat er een groot geheim verborgen lag. Na veel avonturen en uitdagingen ontdekten ze het geheim van het eiland en slaagden ze erin om de betovering te verbreken. Ze keerden terug naar hun schip met nieuwe kennis en verhalen om te delen.");

        // Verhaal 19: De jonge koning
        stories.add("In een ver koninkrijk werd een jonge prins plotseling koning na de dood van zijn vader. Hij was jong en onervaren, maar vastbesloten om een goede heerser te zijn. Met de hulp van zijn raadgevers en vrienden leerde hij snel en nam hij wijze beslissingen. Hij vocht tegen vijanden, verbeterde het leven van zijn volk en werd geliefd door iedereen. Onder zijn leiding bloeide het koninkrijk en leefde het volk in vrede en welvaart.");

        // Verhaal 20: De magische vogel
        stories.add("Er was eens een magische vogel die prachtige liederen zong en geluk bracht aan iedereen die hem hoorde. Op een dag werd de vogel gevangen door een boze heks die zijn magie wilde gebruiken voor kwade doeleinden. Een dappere jongen hoorde van de vogel en besloot hem te redden. Na een lange reis vol gevaren vond hij de vogel en bevrijdde hem. De vogel, dankbaar voor zijn vrijheid, zong een lied dat de vloek van de heks verbrak. De jongen keerde terug naar zijn dorp als een held en de vogel bleef voor altijd bij hem, zingend en geluk brengend aan iedereen.");

        return stories;
    }
}
