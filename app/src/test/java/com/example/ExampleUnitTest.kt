package com.example

import com.example.engine.CricketEngine
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testOversParsing() {
        assertEquals(26, CricketEngine.parseOvers("4.2"))
        assertEquals(24, CricketEngine.parseOvers("4.0"))
        assertEquals(24, CricketEngine.parseOvers("4"))
        assertNull(CricketEngine.parseOvers("4.7"))
        assertEquals("4.2", CricketEngine.oversStr(26))
    }

    @Test
    fun testCareerAggAndRating() {
        val entry = MatchPlayerEntry(
            pid = "p1",
            bat = BattingEntry(batted = true, runs = 60, balls = 35, fours = 6, sixes = 2, notOut = true),
            bowl = BowlingEntry(legal = 24, conceded = 18, wickets = 2, maidens = 0),
            field = FieldingEntry(catches = 1),
            fair = 2
        )
        val agg = CricketEngine.addEntryToAgg(CareerAgg(), entry)
        assertEquals(1, agg.matches)
        assertEquals(60, agg.runs)
        assertEquals(2, agg.wickets)
        assertTrue(agg.hsNO)

        val rating = CricketEngine.ratingForSingleFormat(agg, "T20", "overall")
        assertNotNull(rating)
        assertTrue(rating!! in 0.0..100.0)
    }
}
