#ifdef BUFFERED_PROFILES
layout(std140) uniform Profiles0 { uvec4 words0[4096]; };
layout(std140) uniform Profiles1 { uvec4 words1[4096]; };
layout(std140) uniform Profiles2 { uvec4 words2[4096]; };
layout(std140) uniform Profiles3 { uvec4 words3[736]; };
uvec2 profileWords(int pair) {
    int index = (pair/2)%4096;
    uvec4 words;
    if (pair<8192) words = words0[index];
    else if (pair<16384) words = words1[index];
    else if (pair<24576) words = words2[index];
    else words = words3[index];
    return pair%2==0 ? words.xy : words.zw;
}
#endif
const vec2 launch[61] = vec2[61](vec2(-0.11748095,-0.71793914),
vec2(-0.68312997,-0.58740475),
vec2(1.03122167,-0.51778641),
vec2(-0.42206119,-0.46557265),
vec2(0.29152680,-0.26541992),
vec2(0.17839700,-0.20450388),
vec2(-1.00511480,-0.18709929),
vec2(0.15229012,-0.09137407),
vec2(-0.02175573,-0.04786261),
vec2(-0.03045802,0.01305344),
vec2(0.05656490,0.01305344),
vec2(0.04786261,0.06526719),
vec2(-0.42206119,0.07396949),
vec2(-0.26541992,0.21320617),
vec2(0.10877866,0.29152680),
vec2(0.81366436,0.29152680),
vec2(-0.02175573,0.32633597),
vec2(-0.57000017,0.47427495),
vec2(0.26541992,0.50038182),
vec2(-0.13034247,-0.76757235),
vec2(-0.11103248,-0.65171237),
vec2(-0.73860736,-0.63240238),
vec2(1.12480728,-0.55516239),
vec2(-0.57447239,-0.50688740),
vec2(0.98963731,-0.50688740),
vec2(-0.45861241,-0.49723240),
vec2(0.94136232,-0.47792241),
vec2(0.90274233,-0.45861241),
vec2(-0.38137243,-0.42964742),
vec2(-1.08618729,-0.19792746),
vec2(-0.91239732,-0.15930747),
vec2(-0.36206243,0.06275749),
vec2(0.69033237,0.24620245),
vec2(0.88343233,0.32344244),
vec2(0.22689246,0.40068242),
vec2(-0.50688740,0.42964742),
vec2(-0.62274738,0.52619740),
vec2(0.28482245,0.54550739),
vec2(-0.09903141,-0.57671230),
vec2(-0.61166457,-0.53010929),
vec2(-0.08738065,-0.48350628),
vec2(-0.54176004,-0.47185552),
vec2(-0.44855401,-0.40195100),
vec2(0.82137813,-0.40195100),
vec2(-0.05242839,-0.37864949),
vec2(0.69321984,-0.34369723),
vec2(-0.35534799,-0.30874497),
vec2(-0.29709422,-0.30874497),
vec2(-0.04077764,-0.29709422),
vec2(-0.84467964,-0.14563442),
vec2(-0.79807662,-0.13398367),
vec2(-0.65826758,-0.12233291),
vec2(-0.55341080,-0.11068216),
vec2(-0.23884045,0.02912688),
vec2(-0.18058668,0.02912688),
vec2(-0.18058668,0.14563442),
vec2(0.39030025,0.14563442),
vec2(0.50680778,0.18058668),
vec2(-0.01747613,0.19223743),
vec2(0.62331532,0.22718970),
vec2(-0.44855401,0.36699874));
vec4 decodePair(uvec2 words) {
    return vec4(words.x&65535u,words.x>>16u,words.y&65535u,words.y>>16u)-32768.0;
}
#ifdef BUFFERED_PROFILES
#define read0(index) profileWords(0+(index))
#else
#define read0(index) profile0[index]
#endif
#ifdef BUFFERED_PROFILES
#define read1(index) profileWords(6222+(index))
#else
#define read1(index) profile1[index]
#endif
#ifdef BUFFERED_PROFILES
#define read2(index) profileWords(15372+(index))
#else
#define read2(index) profile2[index]
#endif
#ifdef BUFFERED_PROFILES
#define read3(index) profileWords(24522+(index))
#else
#define read3(index) profile3[index]
#endif
vec4 crystal_burst_effect_2Material(vec2 position, float tick) {
    if (tick>=32.0) return vec4(0);
    float spread = 1.0-.84*exp(-.078*tick);
    vec2 field = position/spread;
    int emitter = 0;
    float nearest = 1e8;
    for (int index=0; index<61; ++index) {
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
        light1[group] = sin(power*0.00007730091);
    }
    vec4 light2[6];
    for (int group=0; group<6; ++group) {
        int base = (emitter*6+group)*25;
        vec4 power = decodePair(read1(base));
        for (int channel=0; channel<24; ++channel) {
            power += light1[channel/4][channel%4]*decodePair(read1(base+1+channel));
        }
        light2[group] = sin(power*0.00005818533);
    }
    vec4 light3[6];
    for (int group=0; group<6; ++group) {
        int base = (emitter*6+group)*25;
        vec4 power = decodePair(read2(base));
        for (int channel=0; channel<24; ++channel) {
            power += light2[channel/4][channel%4]*decodePair(read2(base+1+channel));
        }
        light3[group] = sin(power*0.00005025307);
    }
    vec4 light4[1];
    for (int group=0; group<1; ++group) {
        int base = (emitter*1+group)*25;
        vec4 power = decodePair(read3(base));
        for (int channel=0; channel<24; ++channel) {
            power += light3[channel/4][channel%4]*decodePair(read3(base+1+channel));
        }
        light4[group] = (power*0.00002666765);
    }
    vec4 paint = clamp(light4[0],0.0,1.0);
    return vec4(paint.rgb,paint.a*paint.a);
}
