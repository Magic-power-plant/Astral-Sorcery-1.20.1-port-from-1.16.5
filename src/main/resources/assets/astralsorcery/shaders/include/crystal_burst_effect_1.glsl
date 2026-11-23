#ifdef BUFFERED_PROFILES
layout(std140) uniform Profiles0 { uvec4 words0[4096]; };
layout(std140) uniform Profiles1 { uvec4 words1[4096]; };
layout(std140) uniform Profiles2 { uvec4 words2[4096]; };
layout(std140) uniform Profiles3 { uvec4 words3[4096]; };
layout(std140) uniform Profiles4 { uvec4 words4[56]; };
uvec2 profileWords(int pair) {
    int index = (pair/2)%4096;
    uvec4 words;
    if (pair<8192) words = words0[index];
    else if (pair<16384) words = words1[index];
    else if (pair<24576) words = words2[index];
    else if (pair<32768) words = words3[index];
    else words = words4[index];
    return pair%2==0 ? words.xy : words.zw;
}
#endif
const vec2 launch[77] = vec2[77](vec2(-0.45687036,-1.03122167),
vec2(0.54389329,-0.60480934),
vec2(-0.33503827,-0.57870246),
vec2(-0.76145060,-0.43076348),
vec2(-0.60480934,-0.36984744),
vec2(0.10877866,-0.36114514),
vec2(0.07396949,-0.31763368),
vec2(-0.10877866,-0.28282451),
vec2(0.79625977,-0.26541992),
vec2(0.12618324,-0.16099241),
vec2(0.18709929,-0.13488554),
vec2(-0.19580158,-0.05656490),
vec2(-0.02175573,0.02175573),
vec2(0.73534372,0.25671763),
vec2(-0.70923685,0.31763368),
vec2(0.14358783,0.38725202),
vec2(0.19580158,0.38725202),
vec2(0.78755748,0.38725202),
vec2(-0.15229012,0.78755748),
vec2(-0.49723240,-1.12480728),
vec2(-0.41033742,-0.96067232),
vec2(-0.38137243,-0.87377733),
vec2(0.59378239,-0.65171237),
vec2(-0.36206243,-0.62274738),
vec2(0.49723240,-0.54550739),
vec2(-0.30413244,-0.52619740),
vec2(-0.83515734,-0.45861241),
vec2(-0.66136737,-0.39102742),
vec2(0.87377733,-0.28482245),
vec2(0.72895236,-0.24620245),
vec2(0.61309238,0.20758246),
vec2(-0.57447239,0.25585745),
vec2(0.79653735,0.28482245),
vec2(0.13034247,0.33309744),
vec2(-0.77722735,0.35240743),
vec2(0.71929736,0.35240743),
vec2(0.85446734,0.42964742),
vec2(0.20758246,0.43930242),
vec2(-0.13999747,0.69998737),
vec2(-0.16896247,0.87377733),
vec2(-0.35534799,-0.82137813),
vec2(-0.34369723,-0.76312436),
vec2(-0.30874497,-0.73982286),
vec2(-0.30874497,-0.69321984),
vec2(-0.28544346,-0.64661683),
vec2(-0.23884045,-0.54176004),
vec2(0.42525251,-0.49515703),
vec2(-0.27379271,-0.46020477),
vec2(-0.25049120,-0.42525251),
vec2(0.36699874,-0.41360175),
vec2(-0.54176004,-0.33204648),
vec2(0.28544346,-0.32039572),
vec2(-0.47185552,-0.29709422),
vec2(-0.40195100,-0.25049120),
vec2(0.68156909,-0.23884045),
vec2(0.63496607,-0.21553894),
vec2(-0.32039572,-0.20388819),
vec2(0.05242839,-0.20388819),
vec2(0.58836306,-0.20388819),
vec2(0.53010929,-0.19223743),
vec2(0.42525251,-0.15728517),
vec2(-0.22718970,-0.14563442),
vec2(0.23884045,0.07572990),
vec2(0.35534799,0.09903141),
vec2(0.05242839,0.11068216),
vec2(0.40195100,0.13398367),
vec2(0.47185552,0.16893593),
vec2(0.08738065,0.18058668),
vec2(0.55341080,0.19223743),
vec2(0.11068216,0.25049120),
vec2(0.54176004,0.27379271),
vec2(-0.61166457,0.28544346),
vec2(0.61166457,0.30874497),
vec2(-0.07572990,0.40195100),
vec2(-0.09903141,0.49515703),
vec2(-0.11068216,0.54176004),
vec2(-0.11068216,0.61166457));
vec4 decodePair(uvec2 words) {
    return vec4(words.x&65535u,words.x>>16u,words.y&65535u,words.y>>16u)-32768.0;
}
#ifdef BUFFERED_PROFILES
#define read0(index) profileWords(0+(index))
#else
#define read0(index) profile0[index]
#endif
#ifdef BUFFERED_PROFILES
#define read1(index) profileWords(7854+(index))
#else
#define read1(index) profile1[index]
#endif
#ifdef BUFFERED_PROFILES
#define read2(index) profileWords(19404+(index))
#else
#define read2(index) profile2[index]
#endif
#ifdef BUFFERED_PROFILES
#define read3(index) profileWords(30954+(index))
#else
#define read3(index) profile3[index]
#endif
vec4 crystal_burst_effect_1Material(vec2 position, float tick) {
    if (tick>=32.0) return vec4(0);
    float spread = 1.0-.84*exp(-.078*tick);
    vec2 field = position/spread;
    int emitter = 0;
    float nearest = 1e8;
    for (int index=0; index<77; ++index) {
        vec2 displacement = field-launch[index];
        float squared = dot(displacement,displacement);
        if (squared<nearest) { nearest=squared; emitter=index; }
    }
    vec2 axis = normalize(launch[emitter]+vec2(1e-10,0.0));
    vec2 delta = (position-launch[emitter]*spread)*80.0;
    vec2 offset = vec2(dot(delta,axis),dot(delta,vec2(-axis.y,axis.x)));
    offset = sign(offset)*log(abs(offset)+sqrt(1.0+offset*offset));
    float age = tick/31.0;
    float phase = age*.5*TAU;
    vec4 light0[4];
    light0[0] = vec4(offset,3.0*age,3.0*length(position));
    light0[1] = vec4(direction(phase),direction(3.0*phase));
    light0[2] = vec4(direction(2.7*offset.x),direction(2.7*offset.y));
    light0[3] = vec4(direction(5.1*offset.x),direction(5.1*offset.y));
    vec4 light1[6];
    for (int group=0; group<6; ++group) {
        int base = (emitter*6+group)*17;
        vec4 power = decodePair(read0(base));
        for (int channel=0; channel<16; ++channel) {
            power += light0[channel/4][channel%4]*decodePair(read0(base+1+channel));
        }
        light1[group] = sin(power*0.00008432689);
    }
    vec4 light2[6];
    for (int group=0; group<6; ++group) {
        int base = (emitter*6+group)*25;
        vec4 power = decodePair(read1(base));
        for (int channel=0; channel<24; ++channel) {
            power += light1[channel/4][channel%4]*decodePair(read1(base+1+channel));
        }
        light2[group] = sin(power*0.00005896224);
    }
    vec4 light3[6];
    for (int group=0; group<6; ++group) {
        int base = (emitter*6+group)*25;
        vec4 power = decodePair(read2(base));
        for (int channel=0; channel<24; ++channel) {
            power += light2[channel/4][channel%4]*decodePair(read2(base+1+channel));
        }
        light3[group] = sin(power*0.00004877456);
    }
    vec4 light4[1];
    for (int group=0; group<1; ++group) {
        int base = (emitter*1+group)*25;
        vec4 power = decodePair(read3(base));
        for (int channel=0; channel<24; ++channel) {
            power += light3[channel/4][channel%4]*decodePair(read3(base+1+channel));
        }
        light4[group] = (power*0.00003052589);
    }
    vec4 paint = clamp(light4[0],0.0,1.0);
    return vec4(paint.rgb,paint.a*paint.a);
}
