package com.apk.editor.utils.apkSigner;

import com.apk.editor.BuildConfig;

import java.nio.charset.StandardCharsets;

/*
 * Created by APK Explorer & Editor <apkeditor@protonmail.com> on March 04, 2021
 * Based on the original work of Aefyr for https://github.com/Aefyr/PseudoApkSigner
 */
class SignatureFileGenerator {

    private final ManifestGenerator mManifest;
    private final String mHashingAlgorithm;

    SignatureFileGenerator(ManifestGenerator manifestGenerator) {
        mManifest = manifestGenerator;
        mHashingAlgorithm = manifestGenerator.getHashingAlgorithm();
    }

    String generate() throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(generateHeader().toString());

        for (ManifestGenerator.ManifestEntry manifestEntry : mManifest.getEntries()) {
            ManifestGenerator.ManifestEntry sfEntry = new ManifestGenerator.ManifestEntry();
            sfEntry.setAttribute("Name", manifestEntry.getAttribute());
            sfEntry.setAttribute(mHashingAlgorithm + "-Digest", APKSignerUtils.base64Encode(APKSignerUtils.hash(manifestEntry.toString().getBytes(StandardCharsets.UTF_8), mHashingAlgorithm)));
            stringBuilder.append(sfEntry.toString());
        }

        return stringBuilder.toString();
    }

    private ManifestGenerator.ManifestEntry generateHeader() throws Exception {
        ManifestGenerator.ManifestEntry header = new ManifestGenerator.ManifestEntry();
        header.setAttribute("Signature-Version", "1.0");
        header.setAttribute("Created-By", String.format("APK Editor %s", BuildConfig.VERSION_NAME));
        header.setAttribute(mHashingAlgorithm + "-Digest-Manifest", APKSignerUtils.base64Encode(APKSignerUtils.hash(mManifest.generate().getBytes(StandardCharsets.UTF_8), mHashingAlgorithm)));
        return header;
    }

}
