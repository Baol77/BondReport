package bond.scrape;

public final class CountryNormalizer {

    private CountryNormalizer() {}

    public static String normalize(String country) {
        if (country == null) return "";

        String upper = country.toUpperCase()
            .replace("GREEN", "")
            .replace("BOND", "")
            .replace("BTPI", "ITALY")
            .replace("BTP", "ITALY")
            .replace("FUTURA", "")
            .replace(" PIU'", "")
            .replace("VALORE", "")
            .trim();

        return switch (upper) {
            case "ITALY", "ITALIA", "REPUBLIC OF ITALY", "REPUBBLICA ITALIANA", "ITALYI", "ITALY ITALIA" -> "ITALIA";
            case "GERMANY", "DEUTSCHLAND", "BUNDESREPUBLIK DEUTSCHLAND", "GERMANIA" -> "GERMANIA";
            case "FRANCE", "FRANCIA" -> "FRANCIA";
            case "SPAIN", "ESPANA", "SPAGNA" -> "SPAGNA";
            case "PORTUGAL", "PORTOGALLO" -> "PORTOGALLO";
            case "GREECE", "ELLAS", "GRECIA", "REPUBBLICA GRECA" -> "GRECIA";
            case "IRELAND", "IRLANDA" -> "IRLANDA";
            case "NETHERLANDS", "HOLLAND", "PAESI BASSI", "OLANDA" -> "OLANDA";
            case "BELGIUM", "BELGIO" -> "BELGIO";
            case "AUSTRIA" -> "AUSTRIA";
            case "FINLAND", "FINLANDIA" -> "FINLANDIA";
            case "SWEDEN", "SVEZIA" -> "SVEZIA";
            case "NORWAY", "NORVEGIA" -> "NORVEGIA";
            case "UNITED KINGDOM", "UK", "GREAT BRITAIN", "REGNO UNITO", "GRAN BRETAGNA" -> "REGNO UNITO";
            case "ROMANIA", "RUMANIA" -> "ROMANIA";
            case "POLAND", "POLONIA" -> "POLONIA";
            case "HUNGARY", "UNGHERIA" -> "UNGHERIA";
            case "BULGARIA" -> "BULGARIA";
            case "CROATIA", "CROAZIA" -> "CROAZIA";
            case "SLOVENIA" -> "SLOVENIA";
            case "ESTONIA" -> "ESTONIA";
            case "LATVIA", "LETTONIA" -> "LETTONIA";
            case "LITHUANIA", "LITUANIA" -> "LITUANIA";
            case "CYPRUS", "CIPRO" -> "CIPRO";
            case "TURKEY", "TURCHIA", "TÜRKIYE" -> "TURCHIA";
            default -> "";
        };
    }
}