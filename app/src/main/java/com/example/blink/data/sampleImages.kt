package com.example.blink.data

import com.example.blink.SampleImage
import com.example.blink.SampleCategory
import com.example.blink.FilterInfo
import com.example.blink.FilterPreset

val SAMPLE_IMAGES = listOf(
    SampleImage(
        id = "portrait-sample",
        title = "Portrait Studio",
        subtitle = "Try Cutout & Retouch",
        category = SampleCategory.PORTRAIT,
        url = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=1200&auto=format&fit=crop",
        thumbnail = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=300&auto=format&fit=crop"
    ),
    SampleImage(
        id = "nature-sample",
        title = "Mountain Nature",
        subtitle = "Try Auto Enhance & Film",
        category = SampleCategory.NATURE,
        url = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?q=80&w=1200&auto=format&fit=crop",
        thumbnail = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?q=80&w=300&auto=format&fit=crop"
    ),
    SampleImage(
        id = "urban-sample",
        title = "Urban Architecture",
        subtitle = "Try Vintage & Monochrome",
        category = SampleCategory.URBAN,
        url = "https://images.unsplash.com/photo-1514565131-fce0801e5785?q=80&w=1200&auto=format&fit=crop",
        thumbnail = "https://images.unsplash.com/photo-1514565131-fce0801e5785?q=80&w=300&auto=format&fit=crop"
    )
)

val FILTER_PRESETS = listOf(
    FilterInfo(
        id = FilterPreset.NONE,
        name = "Original",
        description = "No filter applied",
        iconName = "Sparkles"
    ),
    FilterInfo(
        id = FilterPreset.VINTAGE,
        name = "Vintage",
        description = "Warm retro fade and gentle grain",
        iconName = "Film"
    ),
    FilterInfo(
        id = FilterPreset.CINEMATIC,
        name = "Cinematic",
        description = "Moody teal & orange cinema color grade",
        iconName = "Clapperboard"
    ),
    FilterInfo(
        id = FilterPreset.BW,
        name = "B&W",
        description = "High-contrast monochrome drama",
        iconName = "Moon"
    ),
    FilterInfo(
        id = FilterPreset.BRIGHT,
        name = "Vibrant",
        description = "Clean brightness and vivid colors",
        iconName = "Sun"
    )
)