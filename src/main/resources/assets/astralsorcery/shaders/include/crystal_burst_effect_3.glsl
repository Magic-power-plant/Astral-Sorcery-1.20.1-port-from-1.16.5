#ifdef BUFFERED_PROFILES
layout(std140) uniform Profiles0 { uvec4 words0[4096]; };
layout(std140) uniform Profiles1 { uvec4 words1[4096]; };
layout(std140) uniform Profiles2 { uvec4 words2[4096]; };
layout(std140) uniform Profiles3 { uvec4 words3[4096]; };
layout(std140) uniform Profiles4 { uvec4 words4[4096]; };
layout(std140) uniform Profiles5 { uvec4 words5[4096]; };
layout(std140) uniform Profiles6 { uvec4 words6[1044]; };
uvec2 profileWords(int pair) {
    int index = (pair/2)%4096;
    uvec4 words;
    if (pair<8192) words = words0[index];
    else if (pair<16384) words = words1[index];
    else if (pair<24576) words = words2[index];
    else if (pair<32768) words = words3[index];
    else if (pair<40960) words = words4[index];
    else if (pair<49152) words = words5[index];
    else words = words6[index];
    return pair%2==0 ? words.xy : words.zw;
}
#endif
const vec2 launch[120] = vec2[120](vec2(-0.52648870,-0.96160333),
vec2(0.34374056,-0.96160333),
vec2(0.78755748,-0.82236665),
vec2(0.21320617,-0.72664143),
vec2(0.92679416,-0.66572538),
vec2(0.87458041,-0.49167953),
vec2(0.37854973,-0.37854973),
vec2(-0.19580158,-0.33503827),
vec2(-0.07396949,-0.31763368),
vec2(0.03916032,-0.16969471),
vec2(-0.38725202,-0.08267178),
vec2(-0.01305344,-0.02175573),
vec2(-0.11748095,0.10877866),
vec2(0.84847353,0.18709929),
vec2(-0.77015289,0.29152680),
vec2(0.85717582,0.41335890),
vec2(0.47427495,0.64832080),
vec2(0.26541992,0.67442768),
vec2(-0.11748095,0.75274831),
vec2(0.78755748,0.83977124),
vec2(-0.57447239,-1.03791230),
vec2(0.37171743,-1.03791230),
vec2(0.86412233,-0.89308733),
vec2(-0.46826741,-0.87377733),
vec2(-0.42964742,-0.81584734),
vec2(0.23654745,-0.77722735),
vec2(0.73860736,-0.77722735),
vec2(0.69998737,-0.74826236),
vec2(0.66136737,-0.70964236),
vec2(0.97998231,-0.70964236),
vec2(0.19792746,-0.64205738),
vec2(0.88343233,-0.63240238),
vec2(0.82550234,-0.59378239),
vec2(0.75791735,-0.56481739),
vec2(0.95101732,-0.51654240),
vec2(0.79653735,-0.44895741),
vec2(0.74826236,-0.41999242),
vec2(0.92205232,0.21723746),
vec2(-0.62274738,0.24620245),
vec2(-0.83515734,0.34275243),
vec2(0.69033237,0.34275243),
vec2(0.74826236,0.38137243),
vec2(0.92205232,0.46826741),
vec2(0.37171743,0.51654240),
vec2(0.21723746,0.55516239),
vec2(0.43930242,0.59378239),
vec2(0.23654745,0.60343738),
vec2(-0.11103248,0.62274738),
vec2(0.66136737,0.69998737),
vec2(0.51654240,0.71929736),
vec2(0.29447744,0.75791735),
vec2(0.71929736,0.76757235),
vec2(-0.13034247,0.84481234),
vec2(0.87377733,0.94136232),
vec2(0.30874497,-0.87963190),
vec2(-0.42525251,-0.76312436),
vec2(1.01944094,-0.72817210),
vec2(-0.37864949,-0.70487059),
vec2(-0.35534799,-0.64661683),
vec2(0.62331532,-0.64661683),
vec2(-0.32039572,-0.60001381),
vec2(0.57671230,-0.58836306),
vec2(0.18058668,-0.56506155),
vec2(0.51845854,-0.56506155),
vec2(-0.28544346,-0.53010929),
vec2(0.73982286,-0.51845854),
vec2(-0.25049120,-0.48350628),
vec2(0.64661683,-0.48350628),
vec2(0.14563442,-0.47185552),
vec2(0.60001381,-0.44855401),
vec2(0.55341080,-0.42525251),
vec2(0.13398367,-0.41360175),
vec2(0.42525251,-0.41360175),
vec2(0.70487059,-0.40195100),
vec2(0.58836306,-0.34369723),
vec2(0.08738065,-0.32039572),
vec2(0.50680778,-0.30874497),
vec2(0.46020477,-0.28544346),
vec2(0.25049120,-0.26214196),
vec2(-0.13398367,-0.23884045),
vec2(0.33204648,-0.20388819),
vec2(0.16893593,-0.16893593),
vec2(-0.43690326,-0.07572990),
vec2(-0.30874497,-0.06407914),
vec2(-0.15728517,-0.06407914),
vec2(-0.22718970,-0.05242839),
vec2(0.27379271,0.02912688),
vec2(0.41360175,0.08738065),
vec2(-0.34369723,0.12233291),
vec2(0.53010929,0.12233291),
vec2(0.58836306,0.13398367),
vec2(0.64661683,0.14563442),
vec2(-0.41360175,0.15728517),
vec2(0.70487059,0.16893593),
vec2(0.40195100,0.18058668),
vec2(0.76312436,0.18058668),
vec2(-0.50680778,0.19223743),
vec2(-0.02912688,0.21553894),
vec2(-0.57671230,0.22718970),
vec2(0.48350628,0.23884045),
vec2(0.54176004,0.25049120),
vec2(0.11068216,0.26214196),
vec2(0.12233291,0.30874497),
vec2(0.21553894,0.30874497),
vec2(0.63496607,0.33204648),
vec2(-0.06407914,0.36699874),
vec2(0.27379271,0.36699874),
vec2(0.37864949,0.40195100),
vec2(0.78642587,0.40195100),
vec2(0.29709422,0.41360175),
vec2(0.16893593,0.42525251),
vec2(0.33204648,0.46020477),
vec2(0.41360175,0.46020477),
vec2(-0.07572990,0.47185552),
vec2(0.47185552,0.48350628),
vec2(0.20388819,0.50680778),
vec2(0.50680778,0.53010929),
vec2(-0.08738065,0.57671230),
vec2(0.60001381,0.65826758),
vec2(-0.11068216,0.66991833));
vec4 decodePair(uvec2 words) {
    return vec4(words.x&65535u,words.x>>16u,words.y&65535u,words.y>>16u)-32768.0;
}
#ifdef BUFFERED_PROFILES
#define read0(index) profileWords(0+(index))
#else
#define read0(index) profile0[index]
#endif
#ifdef BUFFERED_PROFILES
#define read1(index) profileWords(12240+(index))
#else
#define read1(index) profile1[index]
#endif
#ifdef BUFFERED_PROFILES
#define read2(index) profileWords(30240+(index))
#else
#define read2(index) profile2[index]
#endif
#ifdef BUFFERED_PROFILES
#define read3(index) profileWords(48240+(index))
#else
#define read3(index) profile3[index]
#endif
vec4 crystal_burst_effect_3Material(vec2 position, float tick) {
    if (tick>=32.0) return vec4(0);
    float spread = 1.0-.84*exp(-.078*tick);
    vec2 field = position/spread;
    int emitter = 0;
    float nearest = 1e8;
    for (int index=0; index<120; ++index) {
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
        light1[group] = sin(power*0.00010303426);
    }
    vec4 light2[6];
    for (int group=0; group<6; ++group) {
        int base = (emitter*6+group)*25;
        vec4 power = decodePair(read1(base));
        for (int channel=0; channel<24; ++channel) {
            power += light1[channel/4][channel%4]*decodePair(read1(base+1+channel));
        }
        light2[group] = sin(power*0.00006542144);
    }
    vec4 light3[6];
    for (int group=0; group<6; ++group) {
        int base = (emitter*6+group)*25;
        vec4 power = decodePair(read2(base));
        for (int channel=0; channel<24; ++channel) {
            power += light2[channel/4][channel%4]*decodePair(read2(base+1+channel));
        }
        light3[group] = sin(power*0.00005160591);
    }
    vec4 light4[1];
    for (int group=0; group<1; ++group) {
        int base = (emitter*1+group)*25;
        vec4 power = decodePair(read3(base));
        for (int channel=0; channel<24; ++channel) {
            power += light3[channel/4][channel%4]*decodePair(read3(base+1+channel));
        }
        light4[group] = (power*0.00003052622);
    }
    vec4 paint = clamp(light4[0],0.0,1.0);
    return vec4(paint.rgb,paint.a*paint.a);
}
