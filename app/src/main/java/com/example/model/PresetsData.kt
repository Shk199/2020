package com.example.model

object PresetsData {

    // Accurate line-by-line / verse-by-verse segments (آية آية / سطر بسطر)
    val quranVerses = listOf(
        AlignmentSegment(1, 0.000, 3.420, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", 0.995),
        AlignmentSegment(2, 3.820, 7.150, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", 0.992),
        AlignmentSegment(3, 7.550, 10.350, "الرَّحْمَٰنِ الرَّحِيمِ", 0.996),
        AlignmentSegment(4, 10.750, 13.980, "مَالِكِ يَوْمِ الدِّينِ", 0.991),
        AlignmentSegment(5, 14.380, 18.420, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", 0.994),
        AlignmentSegment(6, 18.820, 22.350, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", 0.993),
        AlignmentSegment(7, 22.750, 32.500, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", 0.998)
    )

    val quranSegments get() = quranVerses

    val defaultQuranPreset = AudioPreset(
        title = "تلاوة سورة الفاتحة",
        fileName = "تلاوة_سورة_الفاتحة.wav",
        formatTag = "WAV 48kHz",
        sizeMb = 4.2,
        durationSec = 34.0,
        sampleRate = 48000,
        referenceText = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\nالْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ\nالرَّحْمَٰنِ الرَّحِيمِ\nمَالِكِ يَوْمِ الدِّينِ\nإِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ\nاهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ\nصِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ",
        description = "تلاوة قرآنية مقسمة آية بآية مع فواصل زمنية طبيعية",
        sampleSegments = quranVerses
    )

    val podcastLines = listOf(
        AlignmentSegment(1, 0.000, 3.200, "أهلاً بكم في بودكاست المطورين التقني", 0.991),
        AlignmentSegment(2, 3.600, 7.450, "اليوم سنناقش تقنيات محاذاة الصوت مع النصوص", 0.988),
        AlignmentSegment(3, 7.850, 12.100, "وكيفية استخراج التوقيتات بدقة الفونيمات", 0.994)
    )

    val podcastPreset = AudioPreset(
        title = "مقدمة بودكاست تقني",
        fileName = "podcast_intro.mp3",
        formatTag = "MP3 320kbps",
        sizeMb = 2.1,
        durationSec = 14.0,
        sampleRate = 44100,
        referenceText = "أهلاً بكم في بودكاست المطورين التقني\nاليوم سنناقش تقنيات محاذاة الصوت مع النصوص\nوكيفية استخراج التوقيتات بدقة الفونيمات",
        description = "مقدمة حوارية سطر بسطر",
        sampleSegments = podcastLines
    )

    val allPresets = listOf(defaultQuranPreset, podcastPreset)

    val initialPayload = AlignmentPayload(
        id = "payload-align-init-01",
        status = "aligned",
        engine = "whisperx-ctc-ar",
        sampleRate = 48000,
        audioFileName = defaultQuranPreset.fileName,
        audioDurationSec = defaultQuranPreset.durationSec,
        executionTimeSec = 0.84,
        segments = quranVerses
    )
}
