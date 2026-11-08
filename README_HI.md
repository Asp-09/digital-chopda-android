# Digital Chopda Android project (starter)

यह Android Studio project आपके `DigitalChopda-Pro.html` को Android WebView में चलाता है और WhatsApp deep-link bridge जोड़ता है।

## अभी क्या शामिल है
- पुराना Digital Chopda Pro HTML ऐप assets में शामिल है।
- Android WebView wrapper।
- ग्राहक सूची में WhatsApp बटन: सेव किए गए नंबर पर बकाया/जमा/उधार का तैयार संदेश खोलता है।
- ग्राहक के मोबाइल नंबर से WhatsApp चैट खोलने का native bridge (`AndroidChopda.openWhatsApp(phone, message)`)।
- Firebase SDK और OTP request का शुरुआती native hook।
- App ID: `com.digitalchopda.app`.

## APK बनाने के लिए
1. Android Studio में इस folder को Open करें।
2. Android SDK 35 और JDK 17 इंस्टॉल रखें।
3. Firebase Console में Android app बनाएं, package name `com.digitalchopda.app` रखें।
4. Authentication में Phone और Email/Password providers enable करें।
5. Firebase से `google-services.json` डाउनलोड करके `app/` folder में रखें।
6. Gradle Sync करें और Build > Build Bundle(s) / APK(s) > Build APK(s) चुनें।

## ईमानदार स्थिति / अभी बाकी
यह **starter source project** है, production-ready APK नहीं। Firebase OTP verification UI, email sign-in/linking UI, Google Drive OAuth consent + token handling, encrypted sync queue, multi-device conflict handling और cloud restore अभी पूरे implement नहीं हैं। Google Drive auto-backup हर entry पर तभी होगा जब OAuth consent और Drive API setup पूरा हो। `google-services.json` और Google OAuth client configuration हर developer/account के लिए अलग होते हैं; इन्हें इस प्रोजेक्ट में hard-code नहीं किया गया है।

## Data model compatibility
मौजूदा HTML का localStorage key `digital_chopda_complete_v1` है। HTML डेटा को Android app से सुरक्षित रूप से sync करने के लिए अगला implementation step है: JavaScript save hook से JSON snapshot native Android layer को देना, फिर authenticated user के Drive `appDataFolder` में versioned backup लिखना। हर save पर सीधे network call न करके retry queue लगानी चाहिए, ताकि offline entries न खोएं और Drive API quota/rapid writes की समस्या न हो।

## WhatsApp
JavaScript से:
```js
AndroidChopda.openWhatsApp("919876543210", "Namaste, aapka baki hisab ...");
```
नंबर में country code रखें; India के लिए 91.
