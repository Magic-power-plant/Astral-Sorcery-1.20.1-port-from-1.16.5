#ifdef BUFFERED_PROFILES
layout(std140) uniform Profiles0 { uvec4 words0[4096]; };
layout(std140) uniform Profiles1 { uvec4 words1[4096]; };
layout(std140) uniform Profiles2 { uvec4 words2[4096]; };
layout(std140) uniform Profiles3 { uvec4 words3[4096]; };
layout(std140) uniform Profiles4 { uvec4 words4[3685]; };
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
const vec2 launch[94] = vec2[94](vec2(0.31569207,0.03819082),
vec2(0.39621485,0.09563807),
vec2(0.41465432,0.14003301),
vec2(0.34613016,0.12633320),
vec2(0.51236405,0.20484965),
vec2(0.23226388,0.10474645),
vec2(0.47764048,0.25444399),
vec2(0.18436833,0.10598093),
vec2(0.51743088,0.31670767),
vec2(0.37246429,0.26833449),
vec2(0.10363614,0.07600498),
vec2(0.41465432,0.32129216),
vec2(0.29191621,0.23016883),
vec2(0.18925179,0.17474432),
vec2(0.20980470,0.24446920),
vec2(0.14730968,0.20476059),
vec2(0.23668221,0.37554967),
vec2(0.09530155,0.16297271),
vec2(0.32413075,0.56406687),
vec2(0.07185181,0.12945396),
vec2(0.12455252,0.43560575),
vec2(0.06080818,0.21891245),
vec2(0.06992247,0.31670767),
vec2(0.02026939,0.12459205),
vec2(0.10446969,0.68342514),
vec2(0.01253636,0.34373454),
vec2(-0.08652968,0.62750583),
vec2(-0.00402663,0.02005781),
vec2(-0.13238158,0.64407331),
vec2(-0.07219702,0.22231562),
vec2(-0.14790171,0.30968450),
vec2(-0.23444594,0.37429088),
vec2(-0.27056633,0.40369425),
vec2(-0.16863655,0.25089828),
vec2(-0.15715576,0.20118966),
vec2(-0.13789999,0.13207162),
vec2(-0.32003922,0.27780581),
vec2(-0.48656834,0.34148853),
vec2(-0.39158576,0.24687083),
vec2(-0.27708258,0.17168557),
vec2(-0.14011979,0.08075623),
vec2(-0.46729679,0.20943078),
vec2(-0.33788949,0.11889701),
vec2(-0.19915872,0.06638624),
vec2(-0.34152653,0.07301355),
vec2(-0.29267412,0.06241750),
vec2(-0.52649213,0.11173699),
vec2(-0.61284990,0.12118484),
vec2(-0.40086762,0.07254222),
vec2(-0.38327024,0.00450906),
vec2(-0.53898947,-0.04438813),
vec2(-0.18659075,-0.02212875),
vec2(-0.46611379,-0.07523774),
vec2(-0.33848179,-0.06581354),
vec2(-0.51833695,-0.13719823),
vec2(-0.07743002,-0.02852685),
vec2(-0.61710623,-0.35562633),
vec2(-0.18988466,-0.14040470),
vec2(-0.25444399,-0.20621575),
vec2(-0.31954612,-0.30597945),
vec2(-0.35013381,-0.41929605),
vec2(-0.32460731,-0.42380667),
vec2(-0.14081726,-0.31555270),
vec2(-0.06695895,-0.16516540),
vec2(-0.10370842,-0.25701652),
vec2(-0.00416637,-0.01248350),
vec2(-0.18119853,-0.55086389),
vec2(-0.10994798,-0.43624380),
vec2(-0.02987033,-0.12374850),
vec2(-0.12016283,-0.60910122),
vec2(-0.07833373,-0.48382599),
vec2(-0.02212875,-0.28430944),
vec2(-0.01238495,-0.22147030),
vec2(0.02080583,-0.48810843),
vec2(0.04524395,-0.54704052),
vec2(0.07872737,-0.37841954),
vec2(0.06831291,-0.31151741),
vec2(0.14099863,-0.51333937),
vec2(0.07301355,-0.25339996),
vec2(0.05458990,-0.12455772),
vec2(0.24639046,-0.45997356),
vec2(0.40538302,-0.42245177),
vec2(0.18197711,-0.18854346),
vec2(0.09374252,-0.09374252),
vec2(0.49176414,-0.47114195),
vec2(0.24551613,-0.20087684),
vec2(0.13623443,-0.11003214),
vec2(0.55718203,-0.44244279),
vec2(0.35018276,-0.24357065),
vec2(0.30300006,-0.12586156),
vec2(0.22694009,-0.07665405),
vec2(0.14846079,-0.03779301),
vec2(0.55003341,-0.09429763),
vec2(0.45585219,-0.00435510));
vec4 decodePair(uvec2 words) {
    return vec4(words.x&65535u,words.x>>16u,words.y&65535u,words.y>>16u)-32768.0;
}
#ifdef BUFFERED_PROFILES
#define read0(index) profileWords(0+(index))
#else
#define read0(index) profile0[index]
#endif
#ifdef BUFFERED_PROFILES
#define read1(index) profileWords(9588+(index))
#else
#define read1(index) profile1[index]
#endif
#ifdef BUFFERED_PROFILES
#define read2(index) profileWords(23688+(index))
#else
#define read2(index) profile2[index]
#endif
#ifdef BUFFERED_PROFILES
#define read3(index) profileWords(37788+(index))
#else
#define read3(index) profile3[index]
#endif
vec4 gem_crystal_burst_dayMaterial(vec2 position, float tick) {
    if (tick>=33.0) return vec4(0);
    float spread = 1.0-.965*exp(-.23*tick);
    vec2 field = position/spread;
    int emitter = 0;
    float nearest = 1e8;
    for (int index=0; index<94; ++index) {
        vec2 displacement = field-launch[index];
        float squared = dot(displacement,displacement);
        if (squared<nearest) { nearest=squared; emitter=index; }
    }
    vec2 axis = normalize(launch[emitter]+vec2(1e-10,0.0));
    vec2 delta = (position-launch[emitter]*spread)*80.0;
    vec2 offset = vec2(dot(delta,axis),dot(delta,vec2(-axis.y,axis.x)));
    offset = sign(offset)*log(abs(offset)+sqrt(1.0+offset*offset));
    float age = tick/32.0;
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
        light1[group] = sin(power*0.00012266460);
    }
    vec4 light2[6];
    for (int group=0; group<6; ++group) {
        int base = (emitter*6+group)*25;
        vec4 power = decodePair(read1(base));
        for (int channel=0; channel<24; ++channel) {
            power += light1[channel/4][channel%4]*decodePair(read1(base+1+channel));
        }
        light2[group] = sin(power*0.00006181580);
    }
    vec4 light3[6];
    for (int group=0; group<6; ++group) {
        int base = (emitter*6+group)*25;
        vec4 power = decodePair(read2(base));
        for (int channel=0; channel<24; ++channel) {
            power += light2[channel/4][channel%4]*decodePair(read2(base+1+channel));
        }
        light3[group] = sin(power*0.00004791587);
    }
    vec4 light4[1];
    for (int group=0; group<1; ++group) {
        int base = (emitter*1+group)*25;
        vec4 power = decodePair(read3(base));
        for (int channel=0; channel<24; ++channel) {
            power += light3[channel/4][channel%4]*decodePair(read3(base+1+channel));
        }
        light4[group] = (power*0.00002226268);
    }
    vec4 paint = clamp(light4[0],0.0,1.0);
    return vec4(paint.rgb,paint.a*paint.a);
}
