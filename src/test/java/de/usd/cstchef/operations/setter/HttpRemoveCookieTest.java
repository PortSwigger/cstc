package de.usd.cstchef.operations.setter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.HashMap;

import org.javatuples.Triplet;
import org.junit.Before;
import org.junit.Test;

import burp.CstcObjectFactory;
import burp.api.montoya.core.ByteArray;
import de.usd.cstchef.operations.Operation.OperationInfos;
import de.usd.cstchef.utils.UnitTestObjectFactory;
import de.usd.cstchef.Utils;
import de.usd.cstchef.operations.OperationCategory;

@OperationInfos(name = "HttpRemoveCookieTest", category = OperationCategory.SETTER, description = "Test class")
public class HttpRemoveCookieTest extends HttpRemoveCookie {

    // HashMap<Input, Pair<expectedOutput, cookieToRemove, throwsException>>
    HashMap<String, Triplet<String,String,Boolean>> inputs = new HashMap<>();

    @Test
    public void removeTest() throws Exception {
        for (String inp : inputs.keySet()) {
            Triplet<String, String, Boolean> res = inputs.get(inp);
            ByteArray inputArray = factory.createByteArray(inp);
            ByteArray outputArray = factory.createByteArray(res.getValue0());
            this.cookie.setText(res.getValue1());
            if (res.getValue2()) {
                Exception exception = assertThrows(IllegalArgumentException.class, () -> perform(inputArray));
                assertEquals("Cookie not found.", exception.getMessage());
            }
            else{
                assertArrayEquals(outputArray.getBytes(), perform(inputArray).getBytes());
            }
        }
    }

    @Before
    public void setup() {
        CstcObjectFactory factory = new UnitTestObjectFactory();
        this.factory = factory;
        super.factory = factory;
        Utils.factory = new UnitTestObjectFactory();

        // only one cookie set -> test header removal
        String reqIn1 = """
                GET / HTTP/2
                Host: www.google.com
                Cookie: SOCS=CAESHAgBEhJnd3NfMjAyNTA3MjItMF9SQzEaAmRlIAEaBgiA74XEBg
                Accept-Language: en-US,en;q=0.9
                

                """;
        String reqOut1 = """
                GET / HTTP/2
                Host: www.google.com
                Accept-Language: en-US,en;q=0.9
                
                
                """
                ;
        String reqCookie1 = "SOCS";
        Triplet<String, String, Boolean> reqTriplet1 = new Triplet<String,String,Boolean>(reqOut1, reqCookie1, false);

        // remove first cookie
        String reqIn2 = """
                GET / HTTP/2
                Host: www.google.com
                Cookie: SOCS=CAESHAgBEhJnd3NfMjAyNTA3MjItMF9SQzEaAmRlIAEaBgiA74XEBg; NID=525=IQez5M5LxRlkekUed0GSiNXmnysYJRp3qsmwFMrtx_U9roKjB2NXZWq54bFphnamvyUFReEz_JEAcJBmPqG8JfRBRMW9tcpJ2nGqhMkXWezmr7IHKMZYyEcysJI0863-LNBsVHFMWxC6ipHaTl5wR_ACDuMx7ENZ_Gows8gIgB18Snx-UwwPpdzKgKjwzReLUvNaqOlsb-ZHOQ; AEC=AaJma5vvw3hD7iNkrLDswMR9kTcDV2ps1Tvu22YmdomiqWg2Ht9xaLG6od0; __Secure-ENID=29.SE=gfLe3WsygfrrtKiH0jK4ebhDh19nRXB8r_L1SLQFPFmNJrQACOevUrhNCL34aKR_rGcbv-midulC7edgJyilpUb1uxgwPTciLllNskbBEEU0105K3Fa_V4QFOAf6TS7hEVgFhcGV0zTfUU5wP7iKA7FnVB9Ly5s_scmKAjcjTCilxSIB1olLMqrmObDUrJbuplntpt1Eklob1VvTgyTSFE19cVXk5VEVmVRph1BiryciQIzNOvk
                Accept-Language: en-US,en;q=0.9


                """;
        String reqOut2 = """
                GET / HTTP/2
                Host: www.google.com
                Cookie: NID=525=IQez5M5LxRlkekUed0GSiNXmnysYJRp3qsmwFMrtx_U9roKjB2NXZWq54bFphnamvyUFReEz_JEAcJBmPqG8JfRBRMW9tcpJ2nGqhMkXWezmr7IHKMZYyEcysJI0863-LNBsVHFMWxC6ipHaTl5wR_ACDuMx7ENZ_Gows8gIgB18Snx-UwwPpdzKgKjwzReLUvNaqOlsb-ZHOQ; AEC=AaJma5vvw3hD7iNkrLDswMR9kTcDV2ps1Tvu22YmdomiqWg2Ht9xaLG6od0; __Secure-ENID=29.SE=gfLe3WsygfrrtKiH0jK4ebhDh19nRXB8r_L1SLQFPFmNJrQACOevUrhNCL34aKR_rGcbv-midulC7edgJyilpUb1uxgwPTciLllNskbBEEU0105K3Fa_V4QFOAf6TS7hEVgFhcGV0zTfUU5wP7iKA7FnVB9Ly5s_scmKAjcjTCilxSIB1olLMqrmObDUrJbuplntpt1Eklob1VvTgyTSFE19cVXk5VEVmVRph1BiryciQIzNOvk
                Accept-Language: en-US,en;q=0.9

                
                """;
        String reqCookie2 = "SOCS";
        Triplet<String, String, Boolean> reqTriplet2 = new Triplet<String, String, Boolean>(reqOut2, reqCookie2, false);

        // remove cookie somewhere in between
        String reqIn3 = """
                GET / HTTP/2
                Host: www.google.com
                Cookie: SOCS=CAESHAgBEhJnd3NfMjAyNTA3MjItMF9SQzEaAmRlIAEaBgiA74XEBg; NID=525=IQez5M5LxRlkekUed0GSiNXmnysYJRp3qsmwFMrtx_U9roKjB2NXZWq54bFphnamvyUFReEz_JEAcJBmPqG8JfRBRMW9tcpJ2nGqhMkXWezmr7IHKMZYyEcysJI0863-LNBsVHFMWxC6ipHaTl5wR_ACDuMx7ENZ_Gows8gIgB18Snx-UwwPpdzKgKjwzReLUvNaqOlsb-ZHOQ; AEC=AaJma5vvw3hD7iNkrLDswMR9kTcDV2ps1Tvu22YmdomiqWg2Ht9xaLG6od0; __Secure-ENID=29.SE=gfLe3WsygfrrtKiH0jK4ebhDh19nRXB8r_L1SLQFPFmNJrQACOevUrhNCL34aKR_rGcbv-midulC7edgJyilpUb1uxgwPTciLllNskbBEEU0105K3Fa_V4QFOAf6TS7hEVgFhcGV0zTfUU5wP7iKA7FnVB9Ly5s_scmKAjcjTCilxSIB1olLMqrmObDUrJbuplntpt1Eklob1VvTgyTSFE19cVXk5VEVmVRph1BiryciQIzNOvk
                Accept-Language: en-US,en;q=0.9

                
                """;
        String reqOut3 = """
                GET / HTTP/2
                Host: www.google.com
                Cookie: SOCS=CAESHAgBEhJnd3NfMjAyNTA3MjItMF9SQzEaAmRlIAEaBgiA74XEBg; AEC=AaJma5vvw3hD7iNkrLDswMR9kTcDV2ps1Tvu22YmdomiqWg2Ht9xaLG6od0; __Secure-ENID=29.SE=gfLe3WsygfrrtKiH0jK4ebhDh19nRXB8r_L1SLQFPFmNJrQACOevUrhNCL34aKR_rGcbv-midulC7edgJyilpUb1uxgwPTciLllNskbBEEU0105K3Fa_V4QFOAf6TS7hEVgFhcGV0zTfUU5wP7iKA7FnVB9Ly5s_scmKAjcjTCilxSIB1olLMqrmObDUrJbuplntpt1Eklob1VvTgyTSFE19cVXk5VEVmVRph1BiryciQIzNOvk
                Accept-Language: en-US,en;q=0.9

                
                """;
        String reqCookie3 = "NID";
        Triplet<String, String,  Boolean> reqTriplet3 = new Triplet<String, String, Boolean>(reqOut3, reqCookie3, false);

        // remove cookie at the end
        String reqIn4 = """
                GET / HTTP/2
                Host: www.google.com
                Cookie: SOCS=CAESHAgBEhJnd3NfMjAyNTA3MjItMF9SQzEaAmRlIAEaBgiA74XEBg; NID=525=IQez5M5LxRlkekUed0GSiNXmnysYJRp3qsmwFMrtx_U9roKjB2NXZWq54bFphnamvyUFReEz_JEAcJBmPqG8JfRBRMW9tcpJ2nGqhMkXWezmr7IHKMZYyEcysJI0863-LNBsVHFMWxC6ipHaTl5wR_ACDuMx7ENZ_Gows8gIgB18Snx-UwwPpdzKgKjwzReLUvNaqOlsb-ZHOQ; AEC=AaJma5vvw3hD7iNkrLDswMR9kTcDV2ps1Tvu22YmdomiqWg2Ht9xaLG6od0; __Secure-ENID=29.SE=gfLe3WsygfrrtKiH0jK4ebhDh19nRXB8r_L1SLQFPFmNJrQACOevUrhNCL34aKR_rGcbv-midulC7edgJyilpUb1uxgwPTciLllNskbBEEU0105K3Fa_V4QFOAf6TS7hEVgFhcGV0zTfUU5wP7iKA7FnVB9Ly5s_scmKAjcjTCilxSIB1olLMqrmObDUrJbuplntpt1Eklob1VvTgyTSFE19cVXk5VEVmVRph1BiryciQIzNOvk
                Accept-Language: en-US,en;q=0.9

                
                """;
        String reqOut4 = """
                GET / HTTP/2
                Host: www.google.com
                Cookie: SOCS=CAESHAgBEhJnd3NfMjAyNTA3MjItMF9SQzEaAmRlIAEaBgiA74XEBg; NID=525=IQez5M5LxRlkekUed0GSiNXmnysYJRp3qsmwFMrtx_U9roKjB2NXZWq54bFphnamvyUFReEz_JEAcJBmPqG8JfRBRMW9tcpJ2nGqhMkXWezmr7IHKMZYyEcysJI0863-LNBsVHFMWxC6ipHaTl5wR_ACDuMx7ENZ_Gows8gIgB18Snx-UwwPpdzKgKjwzReLUvNaqOlsb-ZHOQ; AEC=AaJma5vvw3hD7iNkrLDswMR9kTcDV2ps1Tvu22YmdomiqWg2Ht9xaLG6od0
                Accept-Language: en-US,en;q=0.9

                
                """;
        String reqCookie4 = "__Secure-ENID";
        Triplet<String, String, Boolean> reqTriplet4 = new Triplet<String, String, Boolean>(reqOut4, reqCookie4, false);

        // Cookie not found exception
        String reqIn5 = """
                GET / HTTP/2
                Host: www.google.com
                Cookie: SOCS=CAESHAgBEhJnd3NfMjAyNTA3MjItMF9SQzEaAmRlIAEaBgiA74XEBg; NID=525=IQez5M5LxRlkekUed0GSiNXmnysYJRp3qsmwFMrtx_U9roKjB2NXZWq54bFphnamvyUFReEz_JEAcJBmPqG8JfRBRMW9tcpJ2nGqhMkXWezmr7IHKMZYyEcysJI0863-LNBsVHFMWxC6ipHaTl5wR_ACDuMx7ENZ_Gows8gIgB18Snx-UwwPpdzKgKjwzReLUvNaqOlsb-ZHOQ; AEC=AaJma5vvw3hD7iNkrLDswMR9kTcDV2ps1Tvu22YmdomiqWg2Ht9xaLG6od0; __Secure-ENID=29.SE=gfLe3WsygfrrtKiH0jK4ebhDh19nRXB8r_L1SLQFPFmNJrQACOevUrhNCL34aKR_rGcbv-midulC7edgJyilpUb1uxgwPTciLllNskbBEEU0105K3Fa_V4QFOAf6TS7hEVgFhcGV0zTfUU5wP7iKA7FnVB9Ly5s_scmKAjcjTCilxSIB1olLMqrmObDUrJbuplntpt1Eklob1VvTgyTSFE19cVXk5VEVmVRph1BiryciQIzNOvk
                Accept-Language: en-US,en;q=0.9

                
                """;
        String reqOut5 = "";
        String reqCookie5 = "thiscookiedoesnotexist";
        Triplet<String, String, Boolean> reqTriplet5 = new Triplet<String, String, Boolean>(reqOut5, reqCookie5, true);


        // first cookie
        String resIn1 = """
                HTTP/2 200 Ok
                Date: Fri, 24 Oct 2025 09:30:04 GMT
                Set-Cookie: __Secure-ENID=28.SE=J_qdr3xXvt6Wu2kN9WgflqTrtMnw3rsVY_cU_ZC4qVBfIfexSpgM-IVPVPM7jB7oT43rebR0-5TIVG-g6COL6bTtxPMu3ZM0HJkYf-P5Y7-qhi_144yjK_kX6w-V1Jg0q3Cgpfi9Turw2oRF7eMrxYPQwO_9el2f-MxCwP-3HblTYASZVEFSF628pEZO3eTqBDze8GzLLguAfZt8c5-aekvh3c9ekf7V3h0qKbY9-E0BvR-u7X0; expires=Mon, 08-Dec-2025 23:15:40 GMT; path=/; domain=.google.com; Secure; HttpOnly; SameSite=lax
                Set-Cookie: NID=525=IQez5M5LxRlkekUed0GSiNXmnysYJRp3qsmwFMrtx_U9roKjB2NXZWq54bFphnamvyUFReEz_JEAcJBmPqG8JfRBRMW9tcpJ2nGqhMkXWezmr7IHKMZYyEcysJI0863-LNBsVHFMWxC6ipHaTl5wR_ACDuMx7ENZ_Gows8gIgB18Snx-UwwPpdzKgKjwzReLUvNaqOlsb-ZHOQ; expires=Mon, 08-Dec-2025 23:15:40 GMT; path=/; domain=.google.com; Secure; HttpOnly; SameSite=lax


                """;
        String resOut1 = """
                HTTP/2 200 Ok
                Date: Fri, 24 Oct 2025 09:30:04 GMT
                Set-Cookie: __Secure-ENID=28.SE=J_qdr3xXvt6Wu2kN9WgflqTrtMnw3rsVY_cU_ZC4qVBfIfexSpgM-IVPVPM7jB7oT43rebR0-5TIVG-g6COL6bTtxPMu3ZM0HJkYf-P5Y7-qhi_144yjK_kX6w-V1Jg0q3Cgpfi9Turw2oRF7eMrxYPQwO_9el2f-MxCwP-3HblTYASZVEFSF628pEZO3eTqBDze8GzLLguAfZt8c5-aekvh3c9ekf7V3h0qKbY9-E0BvR-u7X0; expires=Mon, 08-Dec-2025 23:15:40 GMT; path=/; domain=.google.com; Secure; HttpOnly; SameSite=lax


                """;
        String resCookie1 = "NID";
        Triplet<String, String, Boolean> resTriplet1 = new Triplet<String,String,Boolean>(resOut1, resCookie1, false);

        // second cookie
        String resIn2 = """
                HTTP/2 200 Ok
                Date: Fri, 24 Oct 2025 09:30:04 GMT
                Set-Cookie: __Secure-ENID=28.SE=J_qdr3xXvt6Wu2kN9WgflqTrtMnw3rsVY_cU_ZC4qVBfIfexSpgM-IVPVPM7jB7oT43rebR0-5TIVG-g6COL6bTtxPMu3ZM0HJkYf-P5Y7-qhi_144yjK_kX6w-V1Jg0q3Cgpfi9Turw2oRF7eMrxYPQwO_9el2f-MxCwP-3HblTYASZVEFSF628pEZO3eTqBDze8GzLLguAfZt8c5-aekvh3c9ekf7V3h0qKbY9-E0BvR-u7X0; expires=Mon, 08-Dec-2025 23:15:40 GMT; path=/; domain=.google.com; Secure; HttpOnly; SameSite=lax
                Set-Cookie: NID=525=IQez5M5LxRlkekUed0GSiNXmnysYJRp3qsmwFMrtx_U9roKjB2NXZWq54bFphnamvyUFReEz_JEAcJBmPqG8JfRBRMW9tcpJ2nGqhMkXWezmr7IHKMZYyEcysJI0863-LNBsVHFMWxC6ipHaTl5wR_ACDuMx7ENZ_Gows8gIgB18Snx-UwwPpdzKgKjwzReLUvNaqOlsb-ZHOQ; expires=Mon, 08-Dec-2025 23:15:40 GMT; path=/; domain=.google.com; Secure; HttpOnly; SameSite=lax


                """;
        String resOut2 = """
                HTTP/2 200 Ok
                Date: Fri, 24 Oct 2025 09:30:04 GMT
                Set-Cookie: NID=525=IQez5M5LxRlkekUed0GSiNXmnysYJRp3qsmwFMrtx_U9roKjB2NXZWq54bFphnamvyUFReEz_JEAcJBmPqG8JfRBRMW9tcpJ2nGqhMkXWezmr7IHKMZYyEcysJI0863-LNBsVHFMWxC6ipHaTl5wR_ACDuMx7ENZ_Gows8gIgB18Snx-UwwPpdzKgKjwzReLUvNaqOlsb-ZHOQ; expires=Mon, 08-Dec-2025 23:15:40 GMT; path=/; domain=.google.com; Secure; HttpOnly; SameSite=lax


                """;
        String resCookie2 = "NID";
        Triplet<String, String, Boolean> resTriplet2 = new Triplet<String,String,Boolean>(resOut2, resCookie2, false);

        // Cookie not found exception
        String resIn3 = """
                HTTP/2 200 Ok
                Date: Fri, 24 Oct 2025 09:30:04 GMT
                Set-Cookie: __Secure-ENID=28.SE=J_qdr3xXvt6Wu2kN9WgflqTrtMnw3rsVY_cU_ZC4qVBfIfexSpgM-IVPVPM7jB7oT43rebR0-5TIVG-g6COL6bTtxPMu3ZM0HJkYf-P5Y7-qhi_144yjK_kX6w-V1Jg0q3Cgpfi9Turw2oRF7eMrxYPQwO_9el2f-MxCwP-3HblTYASZVEFSF628pEZO3eTqBDze8GzLLguAfZt8c5-aekvh3c9ekf7V3h0qKbY9-E0BvR-u7X0; expires=Mon, 08-Dec-2025 23:15:40 GMT; path=/; domain=.google.com; Secure; HttpOnly; SameSite=lax
                Set-Cookie: NID=525=IQez5M5LxRlkekUed0GSiNXmnysYJRp3qsmwFMrtx_U9roKjB2NXZWq54bFphnamvyUFReEz_JEAcJBmPqG8JfRBRMW9tcpJ2nGqhMkXWezmr7IHKMZYyEcysJI0863-LNBsVHFMWxC6ipHaTl5wR_ACDuMx7ENZ_Gows8gIgB18Snx-UwwPpdzKgKjwzReLUvNaqOlsb-ZHOQ; expires=Mon, 08-Dec-2025 23:15:40 GMT; path=/; domain=.google.com; Secure; HttpOnly; SameSite=lax


                """;
        String resOut3 = "";
        String resCookie3 = "thiscookiedoesnotexist";
        Triplet<String, String, Boolean> resTriplet3 = new Triplet<String,String,Boolean>(resOut3, resCookie3, true);

        inputs.put(reqIn1, reqTriplet1);
        inputs.put(reqIn2, reqTriplet2);
        inputs.put(reqIn3, reqTriplet3);
        inputs.put(reqIn4, reqTriplet4);
        inputs.put(reqIn5, reqTriplet5);
        inputs.put(resIn1, resTriplet1);
        inputs.put(resIn2, resTriplet2);
        inputs.put(resIn3, resTriplet3);
    }
}
