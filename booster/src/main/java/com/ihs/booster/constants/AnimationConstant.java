package com.ihs.booster.constants;

/**
 * Created by sharp on 15/8/12.
 */
@Deprecated
public class AnimationConstant {
    /**
     * Dash Board. ===========================================
     */
    public static final float MINANGLE = -205f;
    /**
     * Animation Colors. ===========================================
     */
    public static final int[] COLOR_ARRAY = {0Xfffa5843, 0Xfffb6736, 0Xfffc7628, 0Xfffd841b, 0Xfffe930d, 0Xffffa200, 0Xffd9a60a,
            0Xffb3ab14, 0Xff8daf1d, 0Xff67b427, 0Xff41b831, 0Xff34b25a, 0Xff27ac83, 0Xff1aa7ad, 0Xff0da1d6, 0Xff009bff};
    /**
     * Main Scanning Animation. ===========================================
     */
    public static final long SCANNING_RING_STAR = 2000;
    public static final long METER_DIAL_APPEAR_DURATION = 200;
    public static final long SLIDE_SCALE_VALUE_DURATION = 800;
    public static final int RECYCLERVIEW_ITEM_ANIM_INTERVAL = 1000 / 16;
    public static final int RECYCLERVIEW_ITEM_ANIM_DURATION = 160;
    /**
     * Boosting Animation. ===========================================
     */
    public static final int SCALE_BACK_COLOR = 0x66FFFFFF;
    public static final int SCALE_FORE_COLOR = 0xFFFFFFFF;
    // 1. ring_outside scale animation
    public static final long RING_OUTSIDE_START_TIME = 0;
    public static final float RING_OUTSIDE_SIZE_SCALE_FACTORS[] = {0.03f, 1.1f, 0.91f, 1f};
    public static final long RING_OUTSIDE_DURATIONS[] = {550, 510, 470};
    public static final long RING_INSIDE_START_TIME = 90;
    public static final long RING_INSIDE_DURATIONS[] = {510, 510, 460};
    // 3. progress
    public static final long PROGRESS_START_TIME = 320;
    public static final long PROGRESS_DURATION = 150;
    // 4. center highlight
    public static final long CENTER_HIGHLIGHT_START_TIME = 590;
    public static final long CENTER_HIGHLIGHT_DURATION = 50;
    // 5. boosting_percent
    public static final long LAYOUT_DATA_SIZE_APPEAR_ANIM_START_OFFSET = 590;
    public static final float BOOSTING_PERCENT_START_ALPHA = 0f;
    public static final float BOOSTING_PERCENT_END_ALPHA = 1f;
    public static final float BOOSTING_PERCENT_START_SCALE = 0.63f;
    public static final float BOOSTING_PERCENT_END_SCALE = 1f;
    public static final long LAYOUT_DATA_SIZE_APPEAR_ANIM_DURATION = 240;
    // ================
    // 6.1 clean progress
    public static final long MEMORY_SCAN_ANIM_DURATION = 1600 + 1000;
    public static long CLEAN_PROGRESS_DURATION = 3500;
    public static int CLEAN_APP_SLIDE_MAX_COUNT = 6;
    public static long CLEAN_APP_SLIDE_DURATION = CLEAN_PROGRESS_DURATION / CLEAN_APP_SLIDE_MAX_COUNT;
    // 7 ring_full_appear_duration
    public static final long RING_FULL_APPEAR_DURATION = 100;
    // 8 txt_clean_done_disappear
    public static final long TXT_BOOST_INDICATOR_DONE_DURATION = 350;
    /**
     * Cleaning Animation. ===========================================
     */
    public static final long CLEANING_BLADE_ALPHA_DURATION = 300;
    public static final float CLEANING_BLADE_START_ALPHA = 0f;
    public static final float CLEANING_BLADE_END_ALPHA = 1f;
    public static final float CLEANING_BLADE_START_SCALE = 0.36f;
    public static final float CLEANING_BLADE_END_SCALE = 1f;
    public static final long CLEANING_BLADE_APPEAR_SCALE_DURATION = 500;
    public static final long CLEANING_JUNK_SIZE_DURATION_ONE = 800;
    public static final long CLEANING_JUNK_SIZE_DURATION_TWO = 1800;
    public static final long CLEANING_IMG_CLEANING_SCRAP1_DURATION = 2768;
    public static final long CLEANING_IMG_CLEANING_SCRAP1_ALPHA_APPEAR_DURATION = 2767;
    public static final long CLEANING_IMG_CLEANING_SCRAP1_ALPHA_DISPEAR_DURATION = CLEANING_IMG_CLEANING_SCRAP1_DURATION - CLEANING_IMG_CLEANING_SCRAP1_ALPHA_APPEAR_DURATION;
    public static final long CLEANING_IMG_CLEANING_SCRAP2_DURATION = 2318;
    public static final long CLEANING_IMG_CLEANING_SCRAP2_ALPHA_APPEAR_DURATION = 2317;
    public static final long CLEANING_IMG_CLEANING_SCRAP2_ALPHA_DISPEAR_DURATION = CLEANING_IMG_CLEANING_SCRAP2_DURATION - CLEANING_IMG_CLEANING_SCRAP2_ALPHA_APPEAR_DURATION;
    public static final long CLEANING_IMG_CLEANING_SCRAP3_DURATION = 1952;
    public static final long CLEANING_IMG_CLEANING_SCRAP3_ALPHA_APPEAR_DURATION = 1951;
    public static final long CLEANING_IMG_CLEANING_SCRAP3_ALPHA_DISPEAR_DURATION = CLEANING_IMG_CLEANING_SCRAP3_DURATION - CLEANING_IMG_CLEANING_SCRAP3_ALPHA_APPEAR_DURATION;
    public static final long CLEANING_IMG_CLEAN_DONE_APPEAR_DURATION = 700;
    /**
     * Floating ball Animation. ===========================================
     */
    //1 appear
    public static final long FLOATING_BALL_APPEAR_TRANSLATE_DURATION = 200;
    public static final long FLOATING_BALL_DISMISS_DURATION = 200;
    public static final long FLOATING_BALL_APPEAR_SCALE_DURATION = 200;
    public static final long FLOATING_BALL_PIN_DURATION = 500;

    public static final float FLOATING_BALL_APPEAR_START_SCALE = 0.24f;
    public static final float FLOATING_BALL_APPEAR_END_SCALE = 1f;

    public static final float FLOATING_VIEW_APPEAR_START_SCALE = 0.0f;
    public static final float FLOATING_VIEW_APPEAR_END_SCALE = 1f;
    public static final long FLOATING_VIEW_APPEAR_SCALE_DURATION = 300;
    //2 boost
    public static final long ALARM_BOOST_PROGRESS_DURATION = 2500;
    public static final int ALARM_BOOST_APP_SLIDE_MAX_COUNT = 8;
    //3 done
    public static final long FLOATING_VIEW_DISAPPEAR_OFFSET = 500;
    public static final long FLOATING_VIEW_DISAPPEAR_DURATION = 500;
    /**
     * shortcut Animation. ===========================================
     */
    public static final int SHORTCUT_POINTER_MINANGLE = -227;
    public static final int SHORTCUT_POINTER_MAXANGLE = 47;
    public static final float SHORTCUT_BG_APPEAR_START_SCALE = 0.65f;
    public static final float SHORTCUT_BG_APPEAR_END_SCALE = 1f;
    public static final long SHORTCUT_BG_APPEAR_DURATION = 330;
    public static final int SHORTCUT_BG_CLEAN_DURATION = 2250;
    public static final float SHORTCUT_BG_ROTATE_ANGLE = 360f * 10;
    public static final int SHORTCUT_POINTER_SLIDE_DURATION = 700;
    public static final int SHORTCUT_DISAPPEAR_DELAY = 500;
    public static final int SHORTCUT_BG_DISAPPEAR_DURATION = 500;

    /**
     * battery fragment entry Animation. ===========================================
     */
    //thick line
    public static final float COEFFICIENT = 1.0f;
    //appear
    public static final long BRUSH_LINE_APPEAR_START_OFFSET = Float.valueOf(833f * COEFFICIENT).longValue();
    public static final long BRUSH_LINE_APPEAR_DURATION = Float.valueOf((1125f - 833f) * COEFFICIENT).longValue();
    public static final float BRUSH_LINE_APPEAR_FROM_X_SCALE = 0.02f;
    public static final float BRUSH_LINE_APPEAR_TO_X_SCALE = 1.0f;
    public static final float BRUSH_LINE_APPEAR_FROM_ALPHA = 0.0f;
    public static final float BRUSH_LINE_APPEAR_TO_ALPHA = 1.0f;

    public static final long BRUSH_LINE_TRANS_START_OFFSET = Float.valueOf((1333 - 1125) * COEFFICIENT).longValue();//translate
    public static final long BRUSH_LINE_TRANS_DURATION = Float.valueOf((4375 - 1333) * COEFFICIENT).longValue();

    public static final long BRUSH_LINE_DISAPPEAR_START_OFFSET = Float.valueOf((4583 - 4375) * COEFFICIENT).longValue();//disappear
    public static final long BRUSH_LINE_DISAPPEAR_DURATION = Float.valueOf((4875 - 4583) * COEFFICIENT).longValue();
    public static final float BRUSH_LINE_DISAPPEAR_FROM_X_SCALE = 1.0f;
    public static final float BRUSH_LINE_DISAPPEAR_TO_X_SCALE = 0.01f;
    public static final float BRUSH_LINE_DISAPPEAR_FROM_ALPHA = 1.0f;
    public static final float BRUSH_LINE_DISAPPEAR_TO_ALPHA = 0.0f;

    //tail
    public static final long BRUSH_TAIL_APPEAR_START_OFFSET = Float.valueOf((1333 - 1125) * COEFFICIENT).longValue();
    public static final long BRUSH_TAIL_ALPHA_APPEAR_DURATION = Float.valueOf((1458 - 1333) * COEFFICIENT).longValue();
    public static final long BRUSH_TAIL_SCALE_APPEAR_DURATION = Float.valueOf((2833 - 1333) * COEFFICIENT).longValue();
    public static final float BRUSH_TAIL_APPEAR_FROM_ALPHA = 0.0f;
    public static final float BRUSH_TAIL_APPEAR_TO_ALPHA = 1.0f;
    public static final float BRUSH_TAIL_APPEAR_FROM_Y_SCALE = 0.0f;
    public static final float BRUSH_TAIL_APPEAR_TO_Y_SCALE = 1.42f;

    public static final long BRUSH_TAIL_ALPHA_DISAPPEAR_START_OFFSET = Float.valueOf((4250 - 2833) * COEFFICIENT).longValue();
    public static final long BRUSH_TAIL_ALPHA_DISAPPEAR_DURATION = Float.valueOf((4375 - 4250) * COEFFICIENT).longValue();
    public static final long BRUSH_TAIL_SCALE_DISAPPEAR_DURATION = Float.valueOf((4375 - 2833) * COEFFICIENT).longValue();
    public static final float BRUSH_TAIL_DISAPPEAR_FROM_Y_SCALE = 1.42f;
    public static final float BRUSH_TAIL_DISAPPEAR_TO_Y_SCALE = 0.0f;

    public static final float BRUSH_TAIL_DISAPPEAR_FROM_ALPHA = 1.0f;
    public static final float BRUSH_TAIL_DISAPPEAR_TO_ALPHA = 0.0f;


    //bubble
    public static final float[] BUBBLE_ONE_ANIM_ARRAY =
            {1f, 0.5f, 0f, 0f, 50f, 319 - 426f, 9200};
    public static final float[] BUBBLE_TWO_ANIM_ARRAY =
            {1f, 0.3f, 20f, -30f, 0f, 0f, 9200};
    public static final float[] BUBBLE_THREE_ANIM_ARRAY =
            {1f, 0.1f, -15f, 25f, 30, 800 - 854f, 9200};

    //battery fragment
    public static final long BATTERY_LAYOUT_DURATION = Float.valueOf((300) * COEFFICIENT).longValue();
    public static final long BTN_OPTIMIZE_DURATION = Float.valueOf((300) * COEFFICIENT).longValue();
    public static final long BTN_OPTIMIZE_START_OFFSET = Float.valueOf((400) * COEFFICIENT).longValue();
    /**
     * battery fragment Animation. ===========================================
     */
    public static final float BATTERY_LEVEL_ALPHA_LOW = 0.5f;
    public static final float BATTERY_LEVEL_ALPHA_HIGH = 1.0f;

    /**
     * battery kill apps Animation. ===========================================
     */

    public static final long FLOAT_BATTERY_BOOST_SLIDING_IN_DURATION = 300;
    public static final long FLOAT_BATTERY_BOOST_SLIDING_OUT_DURATION = 300;

    //BATTERY kill animation duration factor

    public static final float BATTERY_COMMON_KILL_ANIMATION_DURATION_FACTOR = 0.6f;
    //circle_door
    public static final long CIRCLE_DOOR_ZOOM_DURATION = 340;
    public static final long CIRCLE_DOOR_ZOOM_START_OFFSET = 330;
    public static final float CIRCLE_DOOR_ZOOM_FROM_SCALE = 0.8f;
    public static final float CIRCLE_DOOR_ZOOM_TO_SCALE = 1.08f;
    public static final float CIRCLE_DOOR_ZOOM_FROM_ALPHA = 0.0f;
    public static final float CIRCLE_DOOR_ZOOM_TO_ALPHA = 1.0f;
    public static final long CIRCLE_DOOR_NARROW_DURATION = 205;
    public static final float CIRCLE_DOOR_NARROW_FROM_SCALE = 1.08f;
    public static final float CIRCLE_DOOR_NARROW_TO_SCALE = 1.0f;
    //closed_door
    public static final long CLOSED_DOOR_FIRST_NARROW_DURATION = 250;
    public static final float CLOSED_DOOR_FIRST_NARROW_FROM_Y_SCALE = 1.0f;
    public static final float CLOSED_DOOR_FIRST_NARROW_TO_Y_SCALE = 0.3f;
    public static final float CLOSED_DOOR_FIRST_NARROW_FROM_X_SCALE = 1.0f;
    public static final float CLOSED_DOOR_FIRST_NARROW_TO_X_SCALE = 1.0f;
    public static final float CLOSED_DOOR_FIRST_NARROW_FROM_ALPHA = 1.0f;
    public static final float CLOSED_DOOR_FIRST_NARROW_TO_ALPHA = 0.5f;
    public static final long CLOSED_DOOR_SECOND_NARROW_DURATION = 250;
    public static final float CLOSED_DOOR_SECOND_NARROW_FROM_Y_SCALE = 0.3f;
    public static final float CLOSED_DOOR_SECOND_NARROW_TO_Y_SCALE = 0.0f;
    public static final float CLOSED_DOOR_SECOND_NARROW_FROM_X_SCALE = 1.0f;
    public static final float CLOSED_DOOR_SECOND_NARROW_TO_X_SCALE = 0.0f;
    public static final float CLOSED_DOOR_SECOND_NARROW_FROM_ALPHA = 0.5f;
    public static final float CLOSED_DOOR_SECOND_NARROW_TO_ALPHA = 0.0f;
    //app_icon
    public static final long APP_ICON_ZOOM_DURATION = 400;
    public static final long APP_ICON_ZOOM_START_OFFSET = 0;
    public static final float APP_ICON_ZOOM_FROM_SCALE = 0.4f;
    public static final float APP_ICON_ZOOM_TO_SCALE = 1.0f;
    public static final float APP_ICON_ZOOM_FROM_ALPHA = 0.0f;
    public static final float APP_ICON_ZOOM_TO_ALPHA = 1.0f;
    //brush
    public static final long DURATION_BRUSH = 300;
    public static final long START_OFFTIME_BRUSH = 200;
    public static final int TRANSLATION_BRUSH = 60;
    //out_circle
    public static final long OUT_CIRCLE_ZOOM_DURATION = 300;
    public static final float OUT_CIRCLE_ZOOM_FROM_SCALE = 0.8f;
    public static final float OUT_CIRCLE_ZOOM_TO_SCALE = 1.08f;
    public static final float OUT_CIRCLE_ZOOM_FROM_ALPHA = 0.0f;
    public static final float OUT_CIRCLE_ZOOM_TO_ALPHA = 1.0f;
    public static final long OUT_CIRCLE_NARROW_DURATION = 200;
    public static final float OUT_CIRCLE_NARROW_FROM_SCALE = 1.08f;
    public static final float OUT_CIRCLE_NARROW_TO_SCALE = 1.0f;
    public static final long OUT_CIRCLE_ROTATE_DURATION = APP_ICON_ZOOM_DURATION +
            APP_ICON_ZOOM_START_OFFSET + DURATION_BRUSH + START_OFFTIME_BRUSH + CLOSED_DOOR_FIRST_NARROW_DURATION
            + CLOSED_DOOR_SECOND_NARROW_DURATION + 200;//200为容错时间
    public static final long INNER_CIRCLE_ZOOM_DURATION = 300;
    public static final long INNER_CIRCLE_ZOOM_START_OFFSET = 80;

    //inner_circle
    public static final float INNER_CIRCLE_ZOOM_FROM_SCALE = 0.8f;
    public static final float INNER_CIRCLE_ZOOM_TO_SCALE = 1.08f;
    public static final float INNER_CIRCLE_ZOOM_FROM_ALPHA = 0.0f;
    public static final float INNER_CIRCLE_ZOOM_TO_ALPHA = 1.0f;
    public static final long INNER_CIRCLE_NARROW_DURATION = 200;
    public static final float INNER_CIRCLE_NARROW_FROM_SCALE = 1.08f;
    public static final float INNER_CIRCLE_NARROW_TO_SCALE = 1.0f;
    //background_color
    public static int[] BATTERY_KILL_APPS_COLOR_ARRAY = COLOR_ARRAY;
    public static final long DURATION_BACKGROUND_ANIM = OUT_CIRCLE_ROTATE_DURATION;
    //progressBar
    public static final int OPTIMIZE_PROGRESSBAR_BACKCOLOR = 0x66FFFFFF;
    public static final int OPTIMIZE_PROGRESSBAR_FORECOLOR = 0xFFFFFFFF;
    public static final int PROGRESS_BAR_WIDTH = 10;
    public static final long PROGRESS_BAR_APPEAR_START_OFFSET = 200;
    public static final float PROGRESS_BAR_FROM_SCALE = 0.5f;
    public static final float PROGRESS_BAR_TO_SCALE = 1.0f;
    public static final long PROGRESS_BAR_APPEAR_DURATION = INNER_CIRCLE_ZOOM_DURATION + INNER_CIRCLE_ZOOM_START_OFFSET + INNER_CIRCLE_NARROW_DURATION;
    public static final long PROGRESS_BAR_ROTATE_DURATION = OUT_CIRCLE_ROTATE_DURATION;
    //app_name
    public static final long APP_NAME_MOVE_TO_CENTER_DURATION = APP_ICON_ZOOM_DURATION - 280;//600;
    public static final long APP_NAME_MOVE_TO_CENTER_STARTOFFSET = 0;
    public static final long APP_NAME_MOVE_TO_RIGHT_DURATION = DURATION_BRUSH - 100; //750;
    public static final long APP_NAME_MOVE_TO_RIGHT_OFFSET = START_OFFTIME_BRUSH + APP_ICON_ZOOM_START_OFFSET + 280;//200;
    //disappear
    public static final long DISAPPEAR_DURATION = 500;
    //done
    public static final long DONE_DURATION = 350;
    // 2. ring_inside scale animation
    private static final float INSIDE_FACTOR = 0.9f;
    public static final float RING_INSIDE_SIZE_SCALE_FACTORS[] = {0.03f * INSIDE_FACTOR, 0.97f * INSIDE_FACTOR, 0.90f * INSIDE_FACTOR, 1f * INSIDE_FACTOR, 1f * INSIDE_FACTOR};

    /**
     * ad translate Animation. ===========================================
     */

    public static final int CLEAN_VIEW_TOP_TRANS_DURATION = 400;
    public static final int CLEAN_VIEW_BOTTOM_TRANS_DURATION = 650;
    public static final int AD_TRANS_DURATION = 450;
    public static final int AD_TRANS_START_OFFSET = 500;
    public static final int ROBOT_TRANS_START_OFFSET = 80;
    public static final int TXT_UNIT_TRANS_START_OFFSET = 50;
    public static final int TXT_INDICATOR_TRANS_START_OFFSET = 100;
    public static final int AD_FRAGMENT_LAYOUT_TRANS_START_OFFSET = 100;
    public static final int AD_BACKGROUND_TRANS_START_OFFSET = 140;

    /**
     * float stubborn junk clean. ===========================================
     */
    public static final int STUBBORN_ONE_PERIOD_DURATION = 2000;
    public static final int STUBBORN_APP_ICON_ZOOM_OFFSET = 0;
    public static final int STUBBORN_APP_ICON_DURATION = 350;
    public static final int STUBBORN_SMALL_CHIP_OFFSET = STUBBORN_APP_ICON_ZOOM_OFFSET + STUBBORN_APP_ICON_DURATION - 100;
    public static final int STUBBORN_APP_ICON_NARROW_OFFSET = STUBBORN_ONE_PERIOD_DURATION - 2 * STUBBORN_APP_ICON_DURATION - STUBBORN_APP_ICON_ZOOM_OFFSET;
    public static final int STUBBORN_LAYOUT_DISAPPEAR_DURATION = 350;
    public static final int STUBBORN_DONE_CIRCLE_APPEAR_DURATION = 250;
    public static final int STUBBORN_TXT_AND_IMG_DONE_APPEAR_DURATION = 150;
    public static final int STUBBORN_MAX_PROGRESS = 100;
}
