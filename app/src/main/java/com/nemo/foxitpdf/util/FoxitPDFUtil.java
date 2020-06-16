package com.nemo.foxitpdf.util;

import android.os.Environment;

import com.foxit.uiextensions.home.local.LocalModule;
import com.nemo.foxitpdf.MyApplication;

import java.io.File;
import java.util.HashMap;

public class FoxitPDFUtil {
    private static FoxitPDFUtil instance;
    // The value of "sn" can be found in the "rdk_sn.txt".
    // The value of "key" can be found in the "rdk_key.txt".
    public static final String sn = "h1vooqdJoI2qtvskNT3emNshlcqqO//5f9YZVxPR0bfKBD+IMw4qvw==";
    public static final String key = "ezJvjl3GrGpz9JsXIVWofHV+ZuhFuFa6bsdIEkPzTZ7mhJ96b7hnzVUA6CLaNBBI0EQ3WBxy6AEtJIg6hE7z7yp93cUkacmGJoco6SshnwyDU6Sc23iAOS+46jKZoTEgJVfppscQLpcUxV60iodKugtV19BoMMpKwVajGa8geF9v698ZqvginsCIXg452v1HXaStHnXQ2bskJCvA9xSF4uCEFiTyqbuH5RTVDzkQQ0+5IyMQbPx5YLWdJ58YKpbYiRci2uo7524OFhMhMbaS07eaNSYzhz7+7LVIqDJ5dqNkfCLJtFnwFXQY4l9Ni0Cb/lPhkyBZPFx/cy7+PEuCgUthmJj2ms3WjUc5SA9wvz6CjeC8lcA08hXTRiCXH5VIg+vhJ0+46C0RytkC52Yt7vQ/oUkUcP7ot6WvpwOkjIouaEYOQ5E2H9tkJGF8QRDI0CmArq+GDPAeYG0MedGX36iiy3HOoHFwozM7xVVsHH7RMrgm1pS4nIWv1Lw64zomoH0DuiNbWtMa3A5Os2vZNPkARIv1MTvxoEJ9VRW+7WCgtbJgLE1bf5WMHO6Q3GtPL9jCuV7nrf/C9vc+S7j/5BdBci/brgGWF9jn7aybVS1Y+QK/UProU+PnowyYySxVAjMbab1wxRe9U8dREsOWV3oV6vAunkq2LSkSPWhdfjkgFhHM1MVImqrCVCdWhiuR/AotpL1DpiAzgtOF+e3A7ppUgfDU1heEeZM+OnwzW13NHGICdXn5Xt/KM4B3vs3lxN4kDwmkfbneX1BqRZdBr23Kjx8yPXfc5s4ac0IpINJ06U1vgWdeFc1ipFOkPxi9cxliLw5s/e4O0LKHH/2s9sg4UfTVol7XMkLkAORJkK8P7KBAOOyhTnES9E2pCCGlEZzyPgasNxJ+tEuc26ryh7WQCdZ4nWW3a0nQOwGN9iMZFp7/E1izk0WJuehQSowTp3XGiPjdkiAJyWTuzwPmDXrGMlPIzql7EKUNnxBE1pmF6Bh+r2RQgVTuRaoge/DF2c5hdEI8opaiZAf6cRnXROuOBv9zsRo9C4XpN+Jbgqi3VpiOssXtymT/KjWLihU35PL5SxExMl2zArcTC7u97UZh5a50i+mv9LRAzrncBZz4EM0fef2uH8LByJHL1euAHbKpMhAs1dHqfYiFzWwd3VvXgGN1NAK2z+HmHpYSpMij2j5BPIah4l9EMwaaoCi7txPF9Nnzp+u8Ax8ivv94lRugomYznBKQL5305VF6CN2w3WX1HVFNBQtYHnbhgwFRpkHmt0iRLqGxxq+wiMk=";

    public static final String ADDRESS = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "MyPdf";
}
