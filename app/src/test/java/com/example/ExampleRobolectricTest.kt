package com.example

import com.example.model.AlignmentSegment
import com.example.model.OutputFormat
import com.example.model.PresetsData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

  @Test
  fun testPresetsAndCalculations() {
    val preset = PresetsData.defaultQuranPreset
    assertNotNull(preset)
    assertEquals("تلاوة_سورة_الفاتحة.wav", preset.fileName)
    assertTrue(preset.sampleSegments.isNotEmpty())

    // Verify verse-by-verse alignment (first verse)
    val firstSegment = preset.sampleSegments.first()
    assertEquals("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", firstSegment.text)
    assertEquals(0.0, firstSegment.start, 0.001)
    assertEquals(3.42, firstSegment.end, 0.001)

    // Verify strictly ascending and non-overlapping timing
    for (i in 1 until preset.sampleSegments.size) {
      val prev = preset.sampleSegments[i - 1]
      val curr = preset.sampleSegments[i]
      assertTrue("Segment $i start (${curr.start}) must be >= previous end (${prev.end})", curr.start >= prev.end)
      assertTrue("Segment $i end (${curr.end}) must be > start (${curr.start})", curr.end > curr.start)
    }

    val payload = PresetsData.initialPayload
    val json = payload.toJsonString()
    assertTrue(json.contains("whisperx-ctc-ar"))
    assertTrue(json.contains("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"))

    val srt = payload.toSrtString()
    assertTrue(srt.contains("00:00:00,000 --> 00:00:03,420"))
    assertTrue(srt.contains("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"))
  }
}
