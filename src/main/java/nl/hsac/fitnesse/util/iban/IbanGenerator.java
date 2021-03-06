package nl.hsac.fitnesse.util.iban;

import nl.hsac.fitnesse.util.RandomUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;


/**
 * To select a country and parse the iban request to the proper class
 */
public class IbanGenerator {
    private static final Charset UTF8 = Charset.forName("utf-8");
    protected static final RandomUtil RANDOM_UTIL = new RandomUtil();

    public String generateIban(String country, String bankCode) {
        if (country.equals("")) {
            country = RANDOM_UTIL.randomElement(COUNTRY_CODES);
        }


        switch (country) {
            case "AT":
                return new ATIbanGenerator().generateIban(bankCode);
            case "BE":
                return new BEIbanGenerator().generateIban(bankCode);
            case "BG":
                return new BGIbanGenerator().generateIban(bankCode);
            case "CH":
                return new CHIbanGenerator().generateIban(bankCode);
            case "CY":
                return new CYIbanGenerator().generateIban(bankCode);
            case "CZ":
                return new CZIbanGenerator().generateIban(bankCode);
            case "DE":
                return new DEIbanGenerator().generateIban(bankCode);
            case "DK":
                return new DKIbanGenerator().generateIban(bankCode);
            case "EE":
                return new EEIbanGenerator().generateIban(bankCode);
            case "ES":
                return new ESIbanGenerator().generateIban(bankCode);
            case "FI":
                return new FIIbanGenerator().generateIban(bankCode);
            case "FR":
                return new FRIbanGenerator().generateIban(bankCode);
            case "GB":
                return new GBIbanGenerator().generateIban(bankCode);
            case "GI":
                return new GIIbanGenerator().generateIban(bankCode);
            case "GR":
                return new GRIbanGenerator().generateIban(bankCode);
            case "HR":
                return new HRIbanGenerator().generateIban(bankCode);
            case "LU":
                return new LUIbanGenerator().generateIban(bankCode);
            case "NL":
                return new NLIbanGenerator().generateIban(bankCode);
            default:
                throw new IllegalArgumentException("The given country code " + country + " is not (yet) implemented");
        }
    }

    final static String[] COUNTRY_CODES = {
            "AT",     //Austria
            "BE",   //Belgium
            "BG",   //Bulgaria
            "CH",   //Switzerland
            "CY",   //Cyprus
            "CZ",   //Czech Republic
            "DE",   //Germany
            "DK",   //Denmark
            "EE",   //Estonia
            "ES",   //Spain
            "FI",   //Finland
            "FR",   //France
            "GB",   //United Kingdom
            "GI",   //Gibraltar
            "GR",   //Greece
            "HR",   //Croatia
//            "HU",   //Hungary
//            "IE",   //Ireland
//            "IS",   //Iceland
//            "IT",   //Italy
//            "LI",   //Liechtenstein
//            "LT",   //Lithuania
            "LU",   //Luxembourg
//            "LV",   //Latvia
//            "MC",   //Monaco
//            "MT",   //Malta
            "NL",   //Netherlands
//            "NO",   //Norway
//            "PL",   //Poland
//            "PT",   //Portugal
//            "RO",   //Romania
//            "SE",   //Sweden
//            "SI",   //Slovenia
//            "SK",   //Slovakia
//            "SM",   //San Marino

    };

    /**
     * Convert a capital letter into digits: A -> 10 ... Z -> 35 (ISO 13616).
     *
     * @param str
     * @return
     */
    String stringToNumbersIso13616(String str) {
        char[] letters = str.toUpperCase().toCharArray();
        String result = "";
        String capitals = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int j = 0; j < letters.length; j++)
            for (int i = 0; i < capitals.length(); i++)
                if (letters[j] == capitals.charAt(i)) {
                    result = result + Integer.toString(i);
                }
        return result;
    }

    /**
     * Calculate the control number using the mod97 method
     *
     * @param modString string to calculate number for
     * @return mod97 number
     */
    protected static int mod97(String modString) {
        String part = "";
        int modPart;

        for (int i = 0; i < modString.length(); i++) {
            if (part.length() < 9)
                part = part + modString.charAt(i);
            else {
                modPart = (int) Long.parseLong(part) % 97;
                part = Integer.toString(modPart) + modString.charAt(i);
            }
        }
        return (int) Long.parseLong(part) % 97;
    }

    /**
     * To supply a bankcode based on the bankcode properties and availability of a list
     * It does not check if the bankcode supplied is found in a bankcode list as this might be a new or undocumented bank
     *
     * @param bankCode explicit bank code supplied.
     * @param bankCodesArray all known bank codes.
     * @param bankCodeLength length of bank codes for country.
     * @param bankCodeType whether code must be Numeric or Alphanumeric or both
     * @return bank code to use.
     */
    protected String getBankCode(String bankCode, String[] bankCodesArray, int bankCodeLength, String bankCodeType) {
        if (bankCode.length() == 0) {
            if (bankCodesArray.length > 0) {
                return padWithStartingZeros(RANDOM_UTIL.randomElement(bankCodesArray), bankCodeLength);
            } else if (bankCodeType.equals("N")) {
                return getRandomStringNumeric(bankCodeLength);
            } else if (bankCodeType.equals("A")) {
                return getRandomStringAlphaOnly(bankCodeLength);
            } else {
                return getRandomStringAlphaNumeric(bankCodeLength);
            }
        } else if (bankCode.length() < bankCodeLength) {
            return padWithStartingZeros(bankCode, bankCodeLength);
        } else if (bankCode.length() > bankCodeLength) {
            throw new IllegalArgumentException("The supplied bankcode is longer than the Iban specification allows for");
        } else {
            return bankCode;
        }
    }

    /**
     * method for padding a bankcode or other String with zero's to meet a desired length
     */
    String padWithStartingZeros(String toBePadded, int requiredLength) {
        if (toBePadded.length() > requiredLength) {
            throw new IllegalArgumentException("The string to be padded is longer than the requested length");
        }

        while (requiredLength > toBePadded.length()) {
            toBePadded = "0" + toBePadded;
        }

        return toBePadded;
    }


    /**
     * @param length
     * @return
     */
    String getRandomStringNumeric(int length) {
        String permittedAccountDigits = "0123456789";
        return RANDOM_UTIL.randomString(permittedAccountDigits, length);
    }

    /**
     * @param length
     * @return
     */
    String getRandomStringAlphaOnly(int length) {
        String permittedAccountDigits = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        return RANDOM_UTIL.randomString(permittedAccountDigits, length);
    }

    /**
     * @param length
     * @return
     */
    String getRandomStringAlphaNumeric(int length) {
        String permittedAccountDigits = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        return RANDOM_UTIL.randomString(permittedAccountDigits, length);
    }

    /**
     * @param accountLength length of number to generate
     * @param accountCodeType N for numeric, A for alphanumeric
     * @return random account number.
     */
    protected String getAccount(int accountLength, String accountCodeType) {
        String accountNumber;
        if (accountCodeType.equals("N")) {
            accountNumber = getRandomStringNumeric(accountLength);
        } else {
            accountNumber = getRandomStringAlphaNumeric(accountLength);
        }
        return accountNumber;
    }

    /**
     * @param bankCode bank code for control number.
     * @param account account number for control number.
     * @param countryCode country code.
     * @return control number to use.
     */
    protected String getControlNumber(String bankCode, String account, String countryCode) {
        String baseIbanStr = stringToNumbersIso13616(bankCode + account + countryCode) + "00";
        String controlNr = String.valueOf(98 - mod97(baseIbanStr));

        if (controlNr.length() == 1) {
            controlNr = "0" + controlNr;
        }

        return controlNr;
    }

    protected static String[] readLines(String resourceFile) {
        try (InputStream resource = getResourceAsStream(resourceFile)) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource, UTF8))) {
                List<String> allCodes = reader.lines().collect(Collectors.toList());
                return allCodes.toArray(new String[allCodes.size()]);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    protected static InputStream getResourceAsStream(String resourceFile) throws IOException {
        ClassLoader classLoader = IbanGenerator.class.getClassLoader();
        Enumeration<URL> resources = classLoader.getResources("iban/" + resourceFile);
        URL resource = null;
        while (resources.hasMoreElements()) {
            URL r = resources.nextElement();
            // prefer file:// so a file placed on classpath wins from a packaged one
            if (resource == null || "file".equals(r.getProtocol())) {
                resource = r;
            }
        }
        if (resource == null) {
            throw new IllegalStateException("No resource found: " + resourceFile);
        }
        return resource.openStream();
    }
}
