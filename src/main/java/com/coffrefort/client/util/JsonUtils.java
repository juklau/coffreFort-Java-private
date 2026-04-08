package com.coffrefort.client.util;


import com.coffrefort.client.ApiClient;
import com.coffrefort.client.model.*;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import java.util.ArrayList;
import java.util.List;

public class JsonUtils {

    private static ObjectMapper mapper = new ObjectMapper()
        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


    /**
     * Extrait la valeur texte d'un champ JSON (entre guillemets) à partir d'une chaîne JSON simple
     * @param json      la chaîne JSON brute  ex: {"error":"Dossier introuvable"}
     * @param fieldName le nom du champ à extraire ex: "error"
     * @return          la valeur du champ ex: "Dossier introuvable", ou null si non trouvé
     */
    public static String extractJsonField(String json, String fieldName) {

        // sécurité : évite NullPointerException si l'un des deux est null
        if (json == null || fieldName == null) return null;

        // construit le motif à chercher ex: "error"
        String pattern = "\"" + fieldName + "\"";

        // cherche la position du nom du champ dans le JSON
        // ex: {"error":"Dossier introuvable"} → idx pointe sur "error"
        int idx = json.indexOf(pattern);
        if (idx == -1) return null; // champ absent du JSON

        // cherche le ":" qui sépare la clé de la valeur
        // ex: "error" : "Dossier introuvable"
        //            ↑ colon
        int colon = json.indexOf(":", idx + pattern.length());
        if (colon == -1) return null; // JSON malformé

        // cherche le premier guillemet après ":" → début de la valeur
        // ex: "error":"Dossier introuvable"
        //             ↑ firstQuote
        int firstQuote = json.indexOf("\"", colon);
        if (firstQuote == -1) return null; // valeur non textuelle (nombre, booléen...)

        // cherche le guillemet fermant → fin de la valeur
        // ex: "error":"Dossier introuvable"
        //                                 ↑ secondQuote
        int secondQuote = json.indexOf("\"", firstQuote + 1);
        if (secondQuote == -1) return null; // JSON malformé, guillemet non fermé

        // extrait le texte entre les deux guillemets
        // ex: substring(firstQuote+1, secondQuote) → "Dossier introuvable"
        return json.substring(firstQuote + 1, secondQuote);
    }


    /**
     * Extrait la valeur numérique (int/long sous forme de String) d'un champ JSON à partir d'une chaîne JSON simple
     * ex: {"user_id": 6} → "6"
     * @param json      la chaîne JSON brute  ex: {"user_id":6}
     * @param fieldName le nom du champ à extraire ex: "user_id"
     * @return          la valeur numérique sous forme de String ex: "6", ou null si non trouvé
     */
    public static String extractJsonNumberField(String json, String fieldName) {

        // sécurité : évite NullPointerException
        if (json == null) return null;

        // construit le motif à chercher ex: "user_id"
        String pattern = "\"" + fieldName + "\"";

        // cherche la position du nom du champ dans le JSON
        // ex: {"user_id":6} → idx pointe sur "user_id"
        int idx = json.indexOf(pattern);
        if (idx == -1) return null; // champ absent du JSON

        // cherche le ":" qui sépare la clé de la valeur
        // ex: "user_id" : 6
        //              ↑ colon
        int colon = json.indexOf(":", idx + pattern.length());
        if (colon == -1) return null; // JSON malformé

        // démarre juste après le ":"
        int i = colon + 1;

        // saute les espaces éventuels entre ":" et le chiffre
        // ex: "user_id":   6  → avance jusqu'au "6"
        while (i < json.length() && Character.isWhitespace(json.charAt(i))) {
            i++;
        }

        // fin de chaîne atteinte sans trouver de valeur
        if (i >= json.length()) return null;

        // mémorise la position de début du nombre
        // ex: {"user_id":6}
        //                ↑ start
        int start = i;

        // avance tant que le caractère est un chiffre ou un signe "-" (nombres négatifs)
        // s'arrête au premier caractère non numérique ex: "," "}" " "
        while (i < json.length()) {
            char c = json.charAt(i);
            if (!Character.isDigit(c) && c != '-') {
                break; // fin du nombre
            }
            i++;
        }

        // aucun chiffre trouvé après le ":" → valeur absente ou non numérique
        if (i == start) return null;

        // extrait le nombre sous forme de String
        // ex: substring(start, i) → "6" ou "-42"
        return json.substring(start, i);
    }


    /**
     * Extrait le tableau JSON (incluant les crochets []) associé à un champ donné
     * en gérant les crochets imbriqués
     * ex: {"files": [{"id":1}, {"id":2}]} → "[{"id":1}, {"id":2}]"
     * @param json      la chaîne JSON brute
     * @param fieldName le nom du champ contenant le tableau ex: "files"
     * @return          le tableau JSON complet avec ses crochets, ou null si non trouvé
     */
    public static String extractJsonArrayField(String json, String fieldName) {

        // sécurité : évite NullPointerException
        if (json == null) return null;

        // construit le motif à chercher ex: "files"
        String pattern = "\"" + fieldName + "\"";

        // cherche la position du nom du champ dans le JSON
        // ex: {"files":[...]} → index pointe sur "files"
        int index = json.indexOf(pattern);
        if (index == -1) return null; // champ absent du JSON

        // cherche le ":" qui sépare la clé du tableau
        // ex: "files" : [...]
        //             ↑ colon
        int colon = json.indexOf(":", index + pattern.length());
        if (colon == -1) return null; // JSON malformé

        // démarre juste après le ":"
        int i = colon + 1;

        // saute les espaces éventuels entre ":" et le crochet ouvrant
        // ex: "files":   [...] → avance jusqu'au "["
        while (i < json.length() && Character.isWhitespace(json.charAt(i))) {
            i++;
        }

        // vérifie que le prochain caractère est bien "[" (début d'un tableau JSON)
        // si c'est autre chose (nombre, string, objet...) → ce n'est pas un tableau
        if (i >= json.length() || json.charAt(i) != '[') {
            return null;
        }

        // mémorise la position du "[" ouvrant
        // ex: {"files":[{"id":1}]}
        //              ↑ start
        int start = i;

        // compteur de crochets imbriqués :
        // démarre à 1 car on vient de trouver le "[" ouvrant
        // ex: [ [ ] ] → bracketCount : 1 → 2 → 1 → 0
        int bracketCount = 1;
        i++; // avance après le "[" ouvrant

        // parcourt le JSON caractère par caractère
        // jusqu'à trouver le "]" fermant qui correspond au "[" ouvrant (bracketCount revient à 0)
        while (i < json.length() && bracketCount > 0) {
            char c = json.charAt(i);

            if (c == '[') {
                bracketCount++; // tableau imbriqué ouvert → on monte le compteur
            } else if (c == ']') {
                bracketCount--; // tableau fermé → on descend le compteur
            }
            i++;
        }

        // bracketCount == 0 → on a trouvé le "]" fermant correspondant
        // extrait le tableau complet incluant les crochets [ ... ]
        // ex: [{"id":1}, {"id":2}]
        if (bracketCount == 0) {
            return json.substring(start, i);
        }

        // bracketCount != 0 → JSON malformé, crochet non fermé
        return null;
    }

    /**
     * Déséchappe une chaîne JSON pour obtenir la valeur lisible côté client
     * ex: "Dossier introuvable\/" → "Dossier introuvable/"
     * ex: "ligne1\\nligne2"       → "ligne1\nligne2"
     * @param s la chaîne JSON échappée
     * @return  la chaîne lisible, ou null si l'entrée est null
     */
    public static String unescapeJsonString(String s) {
        if (s == null) return null;
        return s
                // séquences d'échappement de contrôle
                .replace("\\n", "\n")   // retour à la ligne
                .replace("\\r", "\r")   // retour chariot
                .replace("\\t", "\t")   // tabulation

                // séquences d'échappement JSON classiques
                .replace("\\/", "/")        // slash (optionnel en JSON mais courant)
                .replace("\\\"", "\"")      // guillemet double
                .replace("\\\\", "\\");     // antislash — doit être en DERNIER
        // sinon "\\n" serait d'abord transformé en "\n"
        // puis le "\" restant serait retransformé
    }

    /**
     * Échappe les caractères spéciaux d'une valeur pour l'insérer proprement dans une chaîne JSON
     * ex: valeur : He said "hello"  →  He said \"hello\"
     * ex: valeur : C:\Users\file    →  C:\\Users\\file
     * @param value la valeur brute à échapper
     * @return      la valeur échappée prête à être insérée dans un JSON, ou "" si null
     */
    public static String escapeJson(String value) {
        if (value == null) return "";
        return value
                .replace("\\", "\\\\")  // antislash en PREMIER — sinon les "\" ajoutés
                // par les remplacements suivants seraient re-échappés

                .replace("\"", "\\\""); // guillemet double → \"
    }

    /**
     * Extrait un champ JSON quelle que soit sa valeur (chaîne OU nombre)
     * Essaie d'abord comme une chaîne (entre guillemets) puis comme un nombre
     * ex: {"name":"rapport.pdf"} → "rapport.pdf"  (via extractJsonField)
     * ex: {"user_id":6}          → "6"            (via extractJsonNumberField)
     * @param json      la chaîne JSON brute
     * @param fieldName le nom du champ à extraire
     * @return          la valeur sous forme de String, ou null si non trouvé
     */
    public static String extractJsonFieldAny(String json, String fieldName) {

        // tentative 1 : valeur entre guillemets ex: "name":"rapport.pdf"
        String value = extractJsonField(json, fieldName);

        // tentative 2 : valeur numérique ex: "user_id":6
        if (value == null) {
            value = extractJsonNumberField(json, fieldName);
        }

        return value; // null si le champ est absent ou dans un format non supporté
    }


    /**
     * Parse la réponse JSON des partages (champ "shares" ou tableau direct) en une liste de ShareItem
     * ex: {"shares":[{"id":1,"file_name":"rapport.pdf",...}]} → List<ShareItem>
     * @param json la chaîne JSON brute reçue de l'API
     * @return     la liste des partages parsés, vide si JSON invalide ou aucun partage
     */
    public static List<ShareItem> parseShareItem(String json) {
        System.out.println("JsonUtils - parseShareItem() - JSON reçu: " + json);

        // liste de résultats — retournée vide en cas d'erreur plutôt que null
        List<ShareItem> result = new ArrayList<>();

        // sécurité : JSON null ou vide → liste vide
        if (json == null || json.isBlank()) {
            System.out.println("JsonUtils - JSON vide ou null");
            return result;
        }

        String arrayContent = json.trim();

        // cas 1 : le JSON est un objet  ex: {"shares":[...]}
        // cas 2 : le JSON est un tableau direct ex: [{...},{...}]
        if (arrayContent.startsWith("{")) {
            System.out.println("JsonUtils - JSON est un objet, extraction du champ 'shares'");

            // extrait le tableau associé au champ "shares"
            String sharesArray = extractJsonArrayField(json, "shares");

            if (sharesArray == null) {
                System.err.println("JsonUtils - ERREUR: Impossible d'extraire le champ 'shares'");
                return result; // JSON mal formé ou champ absent
            }

            arrayContent = sharesArray;
            System.out.println("JsonUtils - Tableau 'shares' extrait: " + arrayContent);
        }

        // retire les crochets [] encadrant le tableau
        // ex: [{"id":1},{"id":2}] → {"id":1},{"id":2}
        String trimmed = arrayContent.trim();
        if (trimmed.startsWith("[")) {
            trimmed = trimmed.substring(1); // retire le "["
        }
        if (trimmed.endsWith("]")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1); // retire le "]"
        }

        trimmed = trimmed.trim();

        // tableau vide [] → liste vide
        if (trimmed.isEmpty()) {
            System.out.println("JsonUtils - Tableau vide");
            return result;
        }

        // log des 200 premiers caractères pour éviter de saturer la console sur les grandes réponses
        System.out.println("JsonUtils - Contenu après nettoyage (premiers 200 chars): " +
                (trimmed.length() > 200 ? trimmed.substring(0, 200) + "..." : trimmed));

        // sépare la chaîne en objets JSON individuels
        // ex: {"id":1},{"id":2} → [{"id":1}, {"id":2}]
        List<String> objects = splitJsonObjects(trimmed);
        System.out.println("JsonUtils - Nombre d'objets détectés: " + objects.size());

        // parse chaque objet JSON en ShareItem
        for (int idx = 0; idx < objects.size(); idx++) {
            String o = objects.get(idx).trim();

            // réassure que l'objet est bien encadré par {} (sécurité après le split)
            if (!o.startsWith("{")) {
                o = "{" + o;
            }
            if (!o.endsWith("}")) {
                o = o + "}";
            }

            System.out.println("JsonUtils - Parsing objet " + idx + ": " + o);

            ShareItem item = new ShareItem();

            // --- extraction de chaque champ ---

            // id (numérique) ex: "id":1 → 1
            String id = extractJsonNumberField(o, "id");
            if (id != null) {
                item.setId(Integer.parseInt(id));
                System.out.println("  - id: " + id);
            }

            // file_name : nom du fichier partagé, déséchappé pour l'affichage
            // ex: "file_name":"rapport_final.pdf" → "rapport_final.pdf"
            String fileName = unescapeJsonString(extractJsonField(o, "file_name"));
            item.setResource(fileName != null ? fileName : "Fichier inconnu"); // fallback si absent
            System.out.println("  - file_name: " + fileName);

            // label : description optionnelle du partage
            String label = unescapeJsonString(extractJsonField(o, "label"));
            item.setLabel(label); // peut être null si non défini
            System.out.println("  - label: " + label);

            // expires_at : date d'expiration du lien de partage
            // ex: "expires_at":"2025-12-31 23:59:59" → affiché tel quel, "-" si absent
            String expiresAt = unescapeJsonString(extractJsonField(o, "expires_at"));
            item.setExpiresAt(expiresAt != null ? expiresAt : "-"); // "-" = pas d'expiration
            System.out.println("  - expires_at: " + expiresAt);

            // remaining_uses : nombre de téléchargements restants
            // null = illimité, sinon entier positif
            String remaining = extractJsonNumberField(o, "remaining_uses");
            if (remaining != null && !remaining.isEmpty() && !"null".equalsIgnoreCase(remaining)) {
                item.setRemainingUses(Integer.parseInt(remaining));
                System.out.println("  - remaining_uses: " + remaining);
            } else {
                item.setRemainingUses(null); // null = pas de limite d'utilisation
                System.out.println("  - remaining_uses: null (illimité)");
            }

            // url : lien public de partage généré par le backend
            String url = unescapeJsonString(extractJsonField(o, "url"));
            item.setUrl(url);
            System.out.println("  - url: " + url);

            // is_revoked : 1 = partage révoqué (désactivé), 0 = actif
            // stocké comme entier en JSON → converti en boolean
            String revoked = extractJsonNumberField(o, "is_revoked");
            boolean isRevoked = "1".equals(revoked); // "1" → true, "0" ou null → false
            item.setRevoked(isRevoked);
            System.out.println("  - is_revoked: " + isRevoked);

            result.add(item); // ajoute le ShareItem parsé à la liste
        }

        System.out.println("JsonUtils - Total d'items parsés: " + result.size());
        return result;
    }

    /**
     * Parse la réponse paginée des partages via Jackson (désérialisation automatique)
     * ex: {"shares":[...],"total":42,"page":1} → PagedShareResponse
     * Contrairement à parseShareItem(), utilise Jackson et non le parser manuel
     * @param json la chaîne JSON brute reçue de l'API
     * @return     l'objet PagedShareResponse désérialisé
     * @throws RuntimeException si le JSON est invalide ou ne correspond pas au modèle
     */
    public static PagedShareResponse parsePagedSharesResponse(String json) {
        try {
            // Jackson mappe automatiquement les champs JSON vers les propriétés de PagedShareResponse
            return mapper.readValue(json, PagedShareResponse.class);
        } catch (Exception e) {
            // encapsule l'exception Jackson en RuntimeException avec le message d'origine
            throw new RuntimeException("JSON parse error: " + e.getMessage(), e);
        }
    }

    /**
     * Parse la réponse paginée des fichiers via Jackson
     * ex: {"files":[...],"total":10,"page":1} → PagedFilesResponse
     * @param json la chaîne JSON brute reçue de l'API
     * @return     l'objet PagedFilesResponse désérialisé
     * @throws RuntimeException si le JSON est invalide ou ne correspond pas au modèle
     */
    public static PagedFilesResponse parsePagedFilesResponse(String json) {
        try {
            return mapper.readValue(json, PagedFilesResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("JSON parse error: " + e.getMessage(), e);
        }
    }

    /**
     * Parse la réponse paginée des versions d'un fichier via Jackson
     * ex: {"versions":[...],"total":3,"page":1} → PagedVersionsResponse
     *  exemple:  Jackson lit "total"  → cherche setTotal()  → appelle setTotal(3)!!
     * @param json la chaîne JSON brute reçue de l'API
     * @return     l'objet PagedVersionsResponse désérialisé
     * @throws RuntimeException si le JSON est invalide ou ne correspond pas au modèle
     */
    public static PagedVersionsResponse parsePagedVersionsResponse(String json) {
        try {
            return mapper.readValue(json, PagedVersionsResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("JSON parse error: " + e.getMessage(), e);
        }
    }

    /**
     * Découpe une chaîne contenant plusieurs objets JSON en objets individuels
     * en comptant les accolades ouvrantes et fermantes
     *
     * Plus robuste qu'un simple split(",") car gère les cas où une virgule
     * apparaît à l'intérieur d'une valeur JSON
     * ex: '{"name":"a,b"},{"name":"c"}' → [{"name":"a,b"}, {"name":"c"}]
     *
     * @param content la chaîne contenant plusieurs objets JSON ex: {"id":1},{"id":2}
     * @return        la liste des objets JSON individuels sous forme de String
     */
    private static List<String> splitJsonObjects(String content) {
        List<String> objects = new ArrayList<>();

        // compteur d'accolades imbriquées — 0 = on est entre deux objets
        int braceCount = 0;

        // position de début de l'objet courant
        int start = 0;

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);

            if (c == '{') {
                if (braceCount == 0) {
                    // première accolade d'un nouvel objet → mémorise la position de début
                    // ex: {"id":1},{"id":2}
                    //     ↑ start
                    start = i;
                }
                braceCount++; // accolade ouvrante → on descend dans l'objet

            } else if (c == '}') {
                braceCount--; // accolade fermante → on remonte

                if (braceCount == 0) {
                    // accolade fermante de niveau 0 → fin de l'objet courant
                    // ex: {"id":1},{"id":2}
                    //            ↑ i+1
                    objects.add(content.substring(start, i + 1));
                    // prêt pour le prochain objet
                }
            }
            // les autres caractères (virgules, espaces entre objets) sont ignorés
        }

        return objects;
    }


    /**
     * Parse la réponse JSON paginée des versions (champ "versions") en une liste de VersionEntry
     * ex: {"versions":[{"id":1,"version":1,"size":1024,"created_at":"2025-12-28 13:20:10","checksum":"abc..."}]}
     * → List<VersionEntry>
     * @param json la chaîne JSON brute reçue de l'API
     * @return     la liste des versions parsées, vide si JSON invalide ou aucune version
     */
    public static List<VersionEntry> parseVersionEntriesFromVersionsList(String json) {

        // liste de résultats — retournée vide en cas d'erreur plutôt que null
        List<VersionEntry> result = new ArrayList<>();

        // sécurité : JSON null ou vide → liste vide
        if (json == null || json.isBlank()) {
            System.out.println("JsonUtils - JSON vide ou null");
            return result;
        }

        // le backend renvoie un objet paginé :
        // { "file_id":1, "page":1, "limit":10, "total":3, "versions": [...] }
        // on extrait uniquement le tableau "versions"
        String itemsArray = extractJsonArrayField(json, "versions");
        if (itemsArray == null || itemsArray.isBlank()) {
            System.out.println("'versions' recu du backend vide ou null");
            return result; // champ absent ou tableau vide
        }

        // retire les crochets [] encadrant le tableau
        // ex: [{"id":1},{"id":2}] → {"id":1},{"id":2}
        String trimmed = itemsArray.trim();
        if (trimmed.startsWith("[")) {
            trimmed = trimmed.substring(1); // retire "["
        }
        if (trimmed.endsWith("]")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1); // retire "]"
        }
        trimmed = trimmed.trim();

        // tableau vide [] → liste vide
        if (trimmed.isEmpty()) {
            return result;
        }

        // sépare la chaîne en objets JSON individuels
        // ex: {"id":1,"version":1},{"id":2,"version":2} → 2 objets
        List<String> objects = splitJsonObjects(trimmed);

        // parse chaque objet JSON en VersionEntry
        for (String object : objects) {
            String o = object.trim();

            // réassure que l'objet est bien encadré par {} (sécurité après le split)
            if (!o.startsWith("{")) {
                o = "{" + o;
            }
            if (!o.endsWith("}")) {
                o = o + "}";
            }

            // --- extraction des champs bruts sous forme de String ---

            // id de la version          ex: "id":3 → "3"
            String idString = extractJsonNumberField(o, "id");

            // numéro de version         ex: "version":2 → "2"
            String versionString = extractJsonNumberField(o, "version");

            // taille en bytes           ex: "size":1048576 → "1048576"
            String sizeString = extractJsonNumberField(o, "size");

            // date de création          ex: "created_at":"2025-12-28 13:20:10" → "2025-12-28 13:20:10"
            String createdAtString = unescapeJsonString(extractJsonField(o, "created_at"));

            // checksum SHA-256 en hex   ex: "checksum":"a3f9..." → "a3f9..."
            String checksumHex = unescapeJsonString(extractJsonField(o, "checksum"));

            // --- conversion des String en types Java avec valeur par défaut si absent ---

            // 0 si le champ est absent ou malformé
            int id      = (idString      != null && !idString.isEmpty())      ? Integer.parseInt(idString)      : 0;
            int version = (versionString != null && !versionString.isEmpty()) ? Integer.parseInt(versionString) : 0;
            long size   = (sizeString    != null && !sizeString.isEmpty())    ? Long.parseLong(sizeString)      : 0L;

            // is_current : true si c'est la version active du fichier
            // stocké comme booléen JSON ("true"/"false"), pas comme entier (0/1)
            Boolean isCurrent = false;
            String isCurrentStr = extractJsonField(o, "is_current");
            if (isCurrentStr != null && isCurrentStr.equals("true")) {
                isCurrent = true; // seule "true" active le flag, tout le reste → false
            }

            // crée le VersionEntry et l'ajoute à la liste
            result.add(new VersionEntry(id, version, size, createdAtString, checksumHex, isCurrent));
        }

        return result;
    }

    /**
     * Parse un objet JSON représentant un fichier en un FileEntry
     * ex: {"id":1,"original_name":"rapport.pdf","size":1048576,"created_at":"2025-12-28 13:20:10"}
     * → FileEntry
     * @param json la chaîne JSON brute reçue de l'API
     * @return     le FileEntry parsé, ou null si le JSON est vide
     * @throws IllegalArgumentException si le champ "id" est absent ou invalide (ex: réponse HTML/erreur)
     */
    public static FileEntry parseFileEntry(String json) {

        // sécurité : JSON null ou vide → null plutôt qu'un objet vide
        if (json == null || json.isBlank()) {
            return null;
        }

        // --- id (numérique obligatoire) ---
        // ex: "id":1 → "1" → 1
        // 0 si absent — sera détecté plus bas comme invalide
        String idStr = extractJsonNumberField(json, "id");
        int id = (idStr != null && !idStr.isEmpty()) ? Integer.parseInt(idStr) : 0;

        // --- nom du fichier ---
        // le backend renvoie "original_name" (nom choisi par l'utilisateur)
        // ex: "original_name":"rapport_final.pdf" → "rapport_final.pdf"
        String name = unescapeJsonString(extractJsonField(json, "original_name"));
        if (name == null || name.isBlank()) {
            // fallback sur "name" au cas où le champ backend serait renommé un jour
            name = unescapeJsonString(extractJsonField(json, "name"));
        }

        // garantit que name n'est jamais null (chaîne vide plutôt que null)
        if (name == null) name = "";

        // --- taille en bytes ---
        // ex: "size":1048576 → "1048576" → 1048576L
        // 0 si absent
        String sizeStr = extractJsonNumberField(json, "size");
        long size = (sizeStr != null && !sizeStr.isEmpty()) ? Long.parseLong(sizeStr) : 0L;

        // --- date de création ---
        // ex: "created_at":"2025-12-28 13:20:10" → "2025-12-28 13:20:10"
        // chaîne vide si absent
        String createdAt = unescapeJsonString(extractJsonField(json, "created_at"));
        if (createdAt == null) createdAt = "";

        // --- date de dernière modification ---
        // ex: "updated_at":"2025-12-28 14:00:00" → "2025-12-28 14:00:00"
        // chaîne vide si absent
        String updatedAt = unescapeJsonString(extractJsonField(json, "updated_at"));
        if (updatedAt == null) updatedAt = "";

        // --- validation de l'id ---
        // si id <= 0 : le JSON reçu n'est pas un fichier valide
        // cas typique : le backend a renvoyé une erreur HTML ou un message d'erreur JSON
        // ex: "<html>500 Internal Server Error</html>" → id = 0 → exception
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "parseFileEntry: champ 'id' invalide ou manquant. JSON=" + json
            );
        }

        // crée et retourne le FileEntry avec toutes les données extraites
        return FileEntry.of(id, name, size, createdAt, updatedAt);
    }

//    /**
//     * Parse une liste d'utilisateurs avec quotas
//     * @param json
//     * @return
//     * @throws Exception
//     */
//    public static List<UserQuota> parseUserQuotaList(String json) throws Exception {
//
//        List<UserQuota> list = new ArrayList<>();
//
//        String usersJson = extractJsonArrayField(json, "users");
//        if(usersJson == null || usersJson.isEmpty()) {
//            return list;
//        }
//
//        //Parser chaque user
//        String [] userBlocks = usersJson.split("\\},\\s*\\{");
//
//        for(String block : userBlocks) {
//            block = block.replaceAll("^\\[?\\{?", "").replaceAll("\\}?\\]?$", "");;
//
//            int id = Integer.parseInt(extractJsonField("{" + block + "}", "id"));
//            //String username = extractJsonField("{" + block + "}", "username");
//            String email = extractJsonField("{" + block + "}", "email");
//            long used = Long.parseLong(extractJsonField("{" + block + "}", "used"));
//            long max = Long.parseLong(extractJsonField("{" + block + "}", "max"));
//
//            Boolean isAdmin = false;
//            String isAdminStr = extractJsonField("{" + block + "}", "is_admin");
//            if (isAdminStr != null && isAdminStr.equals("true")) {
//                isAdmin = true;
//            }
//
//            String role = isAdmin ? "admin" : "user";
//
//            list.add(new UserQuota(id, email, used, max, role));
//        }
//        return list;
//    }

    /**
     * Parse la réponse JSON de l'admin contenant la liste des utilisateurs avec leurs quotas
     * ex: {"users":[{"id":1,"email":"admin@test.com","used":1024,"max":10737418240,"is_admin":1},...]}
     * → List<UserQuota>
     * Utilise Jackson (ObjectMapper) contrairement aux autres parsers manuels
     * @param json la chaîne JSON brute reçue de l'API
     * @return     la liste des UserQuota parsés, vide si "users" absent ou vide
     * @throws Exception si le JSON est invalide (Jackson ne peut pas le lire)
     */
    public static List<UserQuota> parseUserQuotaList(String json) throws Exception {
        List<UserQuota> list = new ArrayList<>();

        // Jackson : parse le JSON en arbre de noeuds navigables
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);

        // extrait le tableau "users" depuis l'objet racine
        // ex: {"users":[...]} → noeud tableau
        JsonNode usersNode = root.get("users");
        if (usersNode == null || !usersNode.isArray()) {
            return list; // champ absent ou pas un tableau → liste vide
        }

        // parcourt chaque objet utilisateur du tableau
        for (JsonNode u : usersNode) {

            // id numérique — 0 si absent
            int id = u.path("id").asInt();

            // email — null si absent
            String email = u.path("email").asText(null);

            // espace utilisé en bytes   ex: "used":1048576
            long used = u.path("used").asLong();

            // quota maximum en bytes    ex: "max":10737418240 (10 Go)
            long max = u.path("max").asLong();

            // is_admin : peut être renvoyé comme booléen (true/false)
            // ou comme entier (0/1) ou string ("0"/"1") selon la version de l'API
            boolean isAdmin = false;
            JsonNode isAdminNode = u.get("is_admin");
            if (isAdminNode != null) {
                if (isAdminNode.isBoolean()) {
                    // cas booléen natif JSON : true/false
                    isAdmin = isAdminNode.asBoolean();
                } else {
                    // cas entier ou string : "1"/"0" ou "true"/"false"
                    String s = isAdminNode.asText("");
                    isAdmin = s.equals("1") || s.equalsIgnoreCase("true");
                }
            }

            // convertit le booléen en label lisible pour l'affichage
            String role = isAdmin ? "admin" : "user";

            list.add(new UserQuota(id, email, used, max, role));
        }

        return list;
    }


    // ILS ETAIENT DANS APICLIENT!
    /**
     * DTO (Data Transfer Object) interne pour structurer les données d'un dossier
     * lors du parsing JSON avant la reconstruction de l'arbre de dossiers
     *
     * DTO = objet léger sans logique métier, uniquement pour transporter les données
     * entre le parsing JSON et la construction des NodeItem
     */
    public static class FolderDto {
        public int id;          // identifiant unique du dossier
        public String name;     // nom affiché du dossier
        public Integer parentId; // id du dossier parent, null si dossier racine
    }

    /**
     * Parse un JSON de dossiers (tableau ou objet unique) en liste de FolderDto
     * Parse un JSON de type:
     *   [ { "id":1, "name":"Docs", "parent_id":null }, ... ]
     * ou un seul objet:
     *   { "id":1, "name":"Docs", "parent_id":null, ... }
     */
    /**
     * Parse un JSON de dossiers (tableau ou objet) en liste de FolderDto (id/name/parentId)
     * Parse un JSON de type:
     *   [ { "id":1, "name":"Docs", "parent_id":null }, ... ]
     * ou un seul objet:
     *   { "id":1, "name":"Docs", "parent_id":null, ... }
     * @param json la chaîne JSON brute reçue de l'API
     * @return  la liste des FolderDto parsés, vide si JSON invalide ou vide
     */
    public static List<FolderDto> parseFolders(String json) {
        List<FolderDto> result = new ArrayList<>();
        if (json == null || json.isBlank()) return result;

        String trimmed = json.trim();
        String[] parts;

        if (trimmed.startsWith("[")) {
            // cas tableau : [ {...}, {...} ]
            // retire les crochets encadrants
            // ex: [{"id":1},{"id":2}] → {"id":1},{"id":2}
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
            if (trimmed.isBlank()) return result;

            // découpe grossière sur "},{"  → sépare les objets
            // ex: {"id":1,"name":"Docs"},{"id":2,"name":"Images"} → 2 parties
            // limité ! : ne gère pas les virgules dans les valeurs de champs
            parts = trimmed.split("\\},\\s*\\{");
        } else {
            // cas objet unique : { ... } → tableau d'une seule entrée
            parts = new String[]{ trimmed };
        }

        for (String part : parts) {
            String objet = part.trim();

            // réassure que l'objet est bien encadré par {} après le split
            if (!objet.startsWith("{")) objet = "{" + objet;
            if (!objet.endsWith("}"))   objet = objet + "}";

            FolderDto dto = new FolderDto();

            // id numérique    ex: "id":1 → 1
            String idStr = JsonUtils.extractJsonNumberField(objet, "id");
            if (idStr != null) {
                dto.id = Integer.parseInt(idStr);
            }

            // nom du dossier  ex: "name":"Documents" → "Documents"
            dto.name = JsonUtils.extractJsonField(objet, "name");

            // parent_id : null si dossier racine, sinon id du dossier parent
            // ex: "parent_id":null → dto.parentId = null (racine)
            // ex: "parent_id":2   → dto.parentId = 2
            String parentStr = JsonUtils.extractJsonNumberField(objet, "parent_id");
            if (parentStr != null) {
                dto.parentId = Integer.parseInt(parentStr);
            } else {
                dto.parentId = null; // null → dossier racine (pas de parent)
            }

            result.add(dto);
        }
        return result;
    }

    /**
     * Parse un JSON de fichiers (tableau ou objet unique) en liste de FileEntry
     * Supporte deux formats :
     *   tableau : [{"id":1,"original_name":"rapport.pdf","size":1024,...}, ...]
     *   objet   : {"id":1,"original_name":"rapport.pdf","size":1024,...}
     * @param json la chaîne JSON brute reçue de l'API
     * @return     la liste des FileEntry parsés, vide si JSON invalide ou vide
     */
    private List<FileEntry> parseFiles(String json) {
        List<FileEntry> result = new ArrayList<>();
        if (json == null || json.isBlank()) return result;

        String trimmed = json.trim();
        String[] parts;

        if (trimmed.startsWith("[")) {
            // cas tableau : retire les crochets encadrants
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
            if (trimmed.isBlank()) return result;

            // découpe grossière sur "},{"
            parts = trimmed.split("\\},\\s*\\{");
        } else {
            // cas objet unique
            parts = new String[]{ trimmed };
        }

        // découpe sur "},{"  — utilise filesParts et non parts

        // bug potentiel !!: filesParts redécoupe trimmed même dans le cas objet unique
        // à faire au cas ou: remplacer filesParts par parts pour être cohérent
        String[] filesParts = trimmed.split("\\},\\s*\\{");

        for (String part : filesParts) {
            String objet = part.trim();

            // réassure que l'objet est bien encadré par {}
            if (!objet.startsWith("{")) objet = "{" + objet;
            if (!objet.endsWith("}"))   objet = objet + "}";

            // id numérique         ex: "id":1 → 1, 0 si absent
            String idStr   = JsonUtils.extractJsonNumberField(objet, "id");

            // nom original         ex: "original_name":"rapport.pdf" → "rapport.pdf"
            String name    = JsonUtils.extractJsonField(objet, "original_name");

            // taille en bytes      ex: "size":1048576 → 1048576L, 0 si absent
            String sizeStr = JsonUtils.extractJsonNumberField(objet, "size");

            // date de création     ex: "created_at":"2025-12-28 13:20:10"
            String date    = JsonUtils.extractJsonField(objet, "created_at");

            // date de modification ex: "updated_at":"2025-12-28 14:00:00"
            String updatedDate = JsonUtils.extractJsonField(objet, "updated_at");

            int  id   = (idStr   != null) ? Integer.parseInt(idStr)   : 0;
            long size = (sizeStr != null) ? Long.parseLong(sizeStr)   : 0L;

            if (name == null) {
                // fallback sur "name" au cas où le champ backend serait renommé
                name = JsonUtils.extractJsonField(objet, "name");
            }

            result.add(FileEntry.of(id, name, size, date, updatedDate));
        }
        return result;
    }

















    /**
     * Empêche l’instanciation de la classe utilitaire JsonUtils.
     */
    private JsonUtils() {
        // constructeur privé pour empêcher l'instanciation
    }












}
