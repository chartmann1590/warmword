from google.oauth2 import service_account
from googleapiclient.discovery import build

KEY = r"H:\psyc-app\play-creds\warmword-play-billing.json"
PKG = "com.charles.warmwords"
C = service_account.Credentials.from_service_account_file(KEY,
    scopes=["https://www.googleapis.com/auth/androidpublisher"])
S = build("androidpublisher","v3",credentials=C)

LISTINGS = {
  "es-ES": {
    "title": "WarmWord: Salud mental con IA",
    "shortDescription": "Tu compañera de IA privada en tu dispositivo para apoyo sereno y sin juicios.",
    "fullDescription": (
      "WarmWord es una compañera de salud mental con IA, privada y en tu dispositivo, que escucha sin juzgar. "
      "Habla cuando los pensamientos ansiosos, acelerados o solitarios se sienten demasiado pesados para cargarlos solos. "
      "El modelo de IA funciona enteramente en tu teléfono, así tus conversaciones son solo tuyas.\n\n"
      "Reflexiona y escribe en un diario en un espacio calmado pensado para ayudarte a desenredar el día. "
      "Los insights suaves de ánimo revelan patrones silenciosos con el tiempo, para que notes qué ayuda y qué duele. "
      "Y si alguna vez lo necesitas, WarmWord muestra recursos de crisis y líneas de ayuda en segundos, sin buscar nada.\n\n"
      "Haz que WarmWord sea única personalizando tonos y recordatorios. Mejora a Premium para desbloquear la experiencia "
      "completa, con conversaciones ilimitadas e insights más profundos.\n\n"
      "WarmWord no sustituye la atención profesional. En una emergencia, contacta siempre a los servicios de emergencia locales."
    ),
  },
  "fr-FR": {
    "title": "WarmWord : compagnon IA",
    "shortDescription": "Votre compagnon d'IA privé sur appareil pour un soutien calme et sans jugement.",
    "fullDescription": (
      "WarmWord est un compagnon de santé mentale à base d'IA, privé et sur votre appareil, qui écoute sans juger. "
      "Discutez quand les pensées anxieuses, envahissantes ou solitaires deviennent trop lourdes à porter seul. "
      "Le modèle d'IA fonctionne entièrement sur votre téléphone, vos conversations vous appartiennent donc.\n\n"
      "Réfléchissez et tenez un journal dans un espace calme conçu pour vous aider à démêler la journée. "
      "De douces analyses d'humeur révèlent des schémas silencieux au fil du temps, pour que vous remarquiez ce qui aide et ce qui blesse. "
      "Et si vous en avez besoin, WarmWord affiche des ressources d'aide et de crise en quelques secondes, sans chercher.\n\n"
      "Rendez WarmWord unique en personnalisant les tons et les rappels. Passez à Premium pour débloquer l'expérience complète, "
      "avec des conversations illimitées et des analyses plus approfondies.\n\n"
      "WarmWord ne remplace pas un suivi professionnel. En cas d'urgence, contactez toujours les services d'urgence locaux."
    ),
  },
  "de-DE": {
    "title": "WarmWord: KI-Coach",
    "shortDescription": "Ihr privater KI-Begleiter auf dem Gerät für ruhigen, urteilsfreien Beistand.",
    "fullDescription": (
      "WarmWord ist ein privater, geräteinterner KI-Begleiter für psychische Gesundheit, der ohne zu verurteilen zuhört. "
      "Schreiben Sie, wenn ängstliche, wirre oder einsame Gedanken sich zu schwer zum Alleintragen anfühlen. "
      "Das KI-Modell läuft vollständig auf Ihrem Gerät, sodass Ihre Gespräche Ihnen gehören.\n\n"
      "Reflektieren Sie und führen Sie ein Tagebuch in einem ruhigen Raum, der Ihnen hilft, den Tag zu entwirren. "
      "Sanfte Stimmungsanalysen offenbaren im Lauf der Zeit leise Muster, damit Sie erkennen, was hilft und was weh tut. "
      "Und falls nötig, zeigt WarmWord in Sekunden Krisen- und Hilfsangebote an, ganz ohne Suchen.\n\n"
      "Machen Sie WarmWord mit eigenen Tönen und Erinnerungen zu Ihrem. Holen Sie sich Premium, um das volle Erlebnis freizuschalten, "
      "mit unbegrenzten Gesprächen und tieferen Einblicken.\n\n"
      "WarmWord ersetzt keine professionelle Hilfe. Wählen Sie in einem Notfall immer die örtlichen Notrufdienste."
    ),
  },
  "pt-BR": {
    "title": "WarmWord: IA para bem-estar",
    "shortDescription": "Sua companheira de IA privada no dispositivo para apoio calmo e sem julgamentos.",
    "fullDescription": (
      "WarmWord é uma companheira de saúde mental com IA, privada e no seu dispositivo, que ouve sem julgar. "
      "Converse quando pensamentos ansiosos, acelerados ou solitários ficam pesados demais para carregar sozinho. "
      "O modelo de IA roda inteiramente no seu telefone, então suas conversas são só suas.\n\n"
      "Reflita e escreva num diário em um espaço calmo feito para ajudar você a desembaraçar o dia. "
      "Insights suaves de humor revelam padrões silenciosos com o tempo, para que você perceba o que ajuda e o que machuca. "
      "E se precisar, o WarmWord mostra recursos de crise e apoio em segundos, sem procurar.\n\n"
      "Deixe o WarmWord único personalizando tons e lembretes. Assine o Premium para liberar a experiência completa, "
      "com conversas ilimitadas e insights mais profundos.\n\n"
      "WarmWord não substitui o cuidado profissional. Em uma emergência, contate sempre os serviços de emergência locais."
    ),
  },
  "it-IT": {
    "title": "WarmWord: compagno IA",
    "shortDescription": "Tua compagna di IA privata sul dispositivo per supporto calmo e senza giudizi.",
    "fullDescription": (
      "WarmWord è una compagna di salute mentale con IA, privata e sul tuo dispositivo, che ascolta senza giudicare. "
      "Parla quando pensieri ansiosi, accelerati o solitari diventano troppo pesanti da affrontare da soli. "
      "Il modello IA funziona interamente sul tuo telefono, quindi le tue conversazioni restano tue.\n\n"
      "Rifletti e scrivi in un diario in uno spazio calmo pensato per aiutarti a districare la giornata. "
      "Gentili insights sull'umore rivelano silenziosi schemi nel tempo, così noti cosa aiuta e cosa fa male. "
      "E se serve, WarmWord mostra risorse di crise e aiuto in pochi secondi, senza cercare.\n\n"
      "Rendi WarmWord unica personalizzando toni e promemoria. Passa a Premium per sbloccare l'esperienza completa, "
      "con conversazioni illimitate e approfondimenti più profondi.\n\n"
      "WarmWord non sostituisce le cure professionali. In un'emergenza, contatta sempre i servizi di emergenza locali."
    ),
  },
  "ja-JP": {
    "title": "WarmWord：心のAIケア",
    "shortDescription": "デバイス上で動くプライベートなAI仲間。穏やかで、他人の目を気にせず話せます。",
    "fullDescription": (
      "WarmWordは、プライベートでデバイス上で動くAIメンタルヘルス仲間です。誰もあなたを裁くことなく、話を聞きます。 "
      "不安や焦り、孤独を感じる思考が一人で抱えるには重すぎるとき、いつでもお話しください。 "
      "AIモデルはすべて端末内で動作するため、会話はあなたのもののままです。\n\n"
      "一日をほどく手助けになる、穏やかな空間で振り返りと日記を。 "
      "優しい気分のインサイトが静かなパターンを時間とともに浮かび上がらせ、何が役に立ち何が傷つけるかに気づけます。 "
      "必要なときは、WarmWordが危機や相談窓口の情報を数秒で表示します。探す必要はありません。\n\n"
      "トーンやリマインダーをカスタマイズして、WarmWordをあなただけのものに。 "
      "Premiumにアップグレードすると、無制限の会話やより深いインサイトなど、すべての体験が解放されます。\n\n"
      "WarmWordは専門的なケアの代わりにはなりません。緊急時は必ず地域の救急サービスに連絡してください。"
    ),
  },
  "hi-IN": {
    "title": "WarmWord: मानसिक स्वास्थ्य AI",
    "shortDescription": "आपका निजी, डिवाइस पर चलने वाला AI साथी — शांत और बिना निर्णय के सहायता।",
    "fullDescription": (
      "WarmWord एक निजी, डिवाइस पर चलने वाला AI मानसिक स्वास्थ्य साथी है जो बिना निर्णय लिए सुनता है। "
      "जब चिंता, बेचैनी या अकेलेपन के विचार अकेले ढोने में बहुत भारी लगने लगें, तब बात करें। "
      "AI मॉडल पूरी तरह आपके डिवाइस पर चलता है, इसलिए आपकी बातचीत आपकी ही रहती है।\n\n"
      "एक शांत जगह में प्रतिबिंबित करें और डायरी लिखें, जो दिन को सुलझाने में मदद करे। "
      "कोमल मूड इनसाइट्स समय के साथ चुपचाप पैटर्न दिखाते हैं, ताकि आप पहचान सकें कि क्या मदद करता है और क्या चोट पहुंचाता है। "
      "और अगर कभी ज़रूरत हो, WarmWord संकट और सहायता संसाधनों को सेकंडों में दिखा देता है — बिना खोजे।\n\n"
      "टोन और रिमाइंडर को अनुकूलित करके WarmWord को अपना बनाएं। पूरा अनुभव, असीमित बातचीत और गहरी जानकारी पाने के लिए Premium में अपग्रेड करें।\n\n"
      "WarmWord पेशेवर देखभाल का विकल्प नहीं है। आपातकाल में हमेशा स्थानीय आपातकालीन सेवाओं से संपर्क करें।"
    ),
  },
  "ru-RU": {
    "title": "WarmWord: ИИ для души",
    "shortDescription": "Ваш приватный ИИ-спутник на устройстве — спокойная поддержка без осуждения.",
    "fullDescription": (
      "WarmWord — приватный ИИ-спутник для ментального здоровья, который работает на вашем устройстве и слушает без осуждения. "
      "Общайтесь, когда тревожные, беспокойные или одинокие мысли становятся слишком тяжёлыми, чтобы нести их в одиночку. "
      "ИИ-модель работает полностью на вашем телефоне, поэтому ваши разговоры остаются вашими.\n\n"
      "Размышляйте и ведите дневник в спокойном пространстве, созданном, чтобы помочь вам распутать день. "
      "Мягкие инсайты настроения со временем открывают тихие закономерности, чтобы вы замечали, что помогает, а что причиняет боль. "
      "А если понадобится, WarmWord за секунды покажет кризисные и справочные ресурсы — искать ничего не нужно.\n\n"
      "Сделайте WarmWord своим, настроив тона и напоминания. Перейдите на Premium, чтобы открыть полный опыт, "
      "включая безлимитные разговоры и более глубокие инсайты.\n\n"
      "WarmWord не заменяет профессиональную помощь. При экстренной ситуации всегда обращайтесь в местные экстренные службы."
    ),
  },
  "ko-KR": {
    "title": "WarmWord: AI 마음 케어",
    "shortDescription": "기기 안에서 돌아가는 프라이빗 AI 친구. 판단 없이 편안하게 털어놓으세요.",
    "fullDescription": (
      "WarmWord는 판단 없이 들어주는, 기기 안에서만 돌아가는 프라이빗 AI 정신건강 친구입니다. "
      "불안하거나 마음이 조급하거나 외로운 생각이 혼자서는 너무 무거워질 때 언제든 이야기하세요. "
      "AI 모델은 온전히 내 휴대폰에서 실행되므로 대화는 오직 나만의 것이 됩니다.\n\n"
      "하루를 정리하는 데 도움을 주는 차분한 공간에서 되돌아보고 일기를 쓰세요. "
      "부드러운 기분 인사이트가 시간에 따라 조용한 패턴을 보여주어, 무엇이 도움이 되고 무엇이 아픈지 알아차리게 해줍니다. "
      "필요할 때는 WarmWord가 위기 및 상담 리소스를 단 몇 초 만에 보여줍니다. 찾을 필요가 없습니다.\n\n"
      "말투와 알림을 설정해 WarmWord를 나만의 것으로 만드세요. Premium으로 업그레이드하면 무제한 대화와 더 깊은 인사이트 등 모든 경험이 열립니다.\n\n"
      "WarmWord는 전문적인 치료를 대신하는 것이 아닙니다. 응급 상황에서는 항상 지역 응급 서비스에 연락하세요."
    ),
  },
  "zh-CN": {
    "title": "WarmWord：私人AI心理伙伴",
    "shortDescription": "设备端运行的私密AI伙伴，让你在平静、不被评判中倾诉。",
    "fullDescription": (
      "WarmWord 是一个私密、在设备端运行的 AI 心理健康伙伴，它会不带评判地倾听你。 "
      "当焦虑、慌乱或孤独的念头沉重得难以独自承担时，随时来聊聊。AI 模型完全在你的手机上运行，所以你的对话只属于你。\n\n"
      "在安静的空间里记录与书写日记，帮你理清这一天。温和的情绪洞察会随时间显现出静默的模式，让你注意到什么有帮助、什么会带来伤害。 "
      "需要时，WarmWord 会在几秒内呈现危机与求助资源——无需寻找。\n\n"
      "通过自定义语调和提醒，让 WarmWord 成为独一无二的你的。升级到 Premium，解锁完整体验，包括无限对话与更深的洞察。\n\n"
      "WarmWord 不能替代专业照护。紧急情况下，请务必联系当地急救服务。"
    ),
  },
  "nl-NL": {
    "title": "WarmWord: AI voor welzijn",
    "shortDescription": "Privé AI-maatje op je telefoon voor rustige, oordeelvrije steun.",
    "fullDescription": (
      "WarmWord is een privé, op je apparaat draaiende AI-companion voor mentale gezondheid die zonder oordeel luistert. "
      "Praat wanneer angstige, gejaagde of eenzame gedachten te zwaar zijn om alleen te dragen. "
      "Het AI-model draait volledig op je telefoon, dus je gesprekken blijven van jou.\n\n"
      "Reflecteer en houd een dagboek in een rustige ruimte die je helpt de dag te ontwarren. "
      "Zachte stemmingsinzichten onthullen in de loop van de tijd stille patronen, zodat je merkt wat helpt en wat pijn doet. "
      "En als je het nodig hebt, toont WarmWord in enkele seconden crisis- en hulpmiddelen, zonder te zoeken.\n\n"
      "Maak WarmWord van jou door tonen en herinneringen te personaliseren. Upgrade naar Premium om de volledige ervaring te ontgrendelen, "
      "met onbeperkte gesprekken en diepere inzichten.\n\n"
      "WarmWord vervangt geen professionele zorg. Neem bij een noodgeval altijd contact op met de lokale noodservices."
    ),
  },
  "tr-TR": {
    "title": "WarmWord: AI ruh sağlığı",
    "shortDescription": "Cihazında çalışan özel AI arkadaşın — yargısız ve huzurlu bir destek.",
    "fullDescription": (
      "WarmWord, yargılamadan dinleyen, özel ve cihazında çalışan bir AI ruh sağlığı arkadaşıdır. "
      "Kaygılı, huzursuz veya yalnız düşünceler tek başına taşımak için çok ağırlaştığında istediğin zaman sohbet et. "
      "AI modeli tamamen telefonunda çalışır, böylece konuşmaların sana ait kalır.\n\n"
      "Günü çözmeni sağlayan sakin bir alanda yansıt ve günlük tut. Nazik ruh hali içgörüleri zamanla sessiz kalıpları ortaya çıkarır, "
      "böylece neyin yardım ettiğini ve neyin incittiğini fark edersin. Ve ihtiyacın olursa WarmWord, saniyeler içinde kriz ve yardım kaynaklarını gösterir, aramana gerek kalmaz.\n\n"
      "Tonları ve hatırlatıcıları kişiselleştirerek WarmWord'ü kendine özel yap. Tüm deneyimi, sınırsız sohbetler ve daha derin içgörülerle birlikte açmak için Premium'a yükselt.\n\n"
      "WarmWord profesyonel bakımın yerini tutmaz. Acil bir durumda her zaman yerel acil servisleriyle iletişime geç."
    ),
  },
}
# sanity check lengths
for lang, d in LISTINGS.items():
    assert len(d["title"]) <= 30, (lang, "title", len(d["title"]))
    assert len(d["shortDescription"]) <= 80, (lang, "short", len(d["shortDescription"]))
    print("ok", lang, "title", len(d["title"]), "short", len(d["shortDescription"]))

e = S.edits().insert(packageName=PKG, body={}).execute()
eid = e["id"]
for lang, d in LISTINGS.items():
    try:
        S.edits().listings().update(packageName=PKG, editId=eid, language=lang, body=d).execute()
        print("set", lang)
    except Exception as ex:
        print("FAIL", lang, repr(ex)[:200])
try:
    c = S.edits().commit(packageName=PKG, editId=eid).execute()
    print("COMMITTED", c.get("id"))
except Exception as ex:
    print("COMMIT ERR", repr(ex)[:400])
