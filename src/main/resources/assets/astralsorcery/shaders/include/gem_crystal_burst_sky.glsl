#ifdef BUFFERED_PROFILES
layout(std140) uniform Profiles0 { uvec4 words0[4096]; };
layout(std140) uniform Profiles1 { uvec4 words1[4096]; };
layout(std140) uniform Profiles2 { uvec4 words2[4096]; };
layout(std140) uniform Profiles3 { uvec4 words3[4096]; };
layout(std140) uniform Profiles4 { uvec4 words4[1550]; };
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
const vec2 launch[84] = vec2[84](vec2(0.22045462,0.00432264),
vec2(0.48089585,0.05516452),
vec2(0.07185181,0.01437036),
vec2(0.38581135,0.10737287),
vec2(0.28019122,0.08609784),
vec2(0.42141803,0.16807970),
vec2(0.45157501,0.22930902),
vec2(0.63146262,0.37871605),
vec2(0.12237012,0.09213895),
vec2(0.17133029,0.17133029),
vec2(0.19451879,0.28590171),
vec2(0.21404710,0.35067291),
vec2(0.09301052,0.23254117),
vec2(0.16375273,0.51334875),
vec2(0.07219702,0.30050221),
vec2(0.03187936,0.18348915),
vec2(0.04557900,0.50507105),
vec2(0.00408671,0.21659549),
vec2(-0.01382360,0.42519655),
vec2(-0.03819082,0.57722660),
vec2(-0.01312237,0.09089386),
vec2(-0.11105334,0.68688546),
vec2(-0.08455931,0.38799979),
vec2(-0.12586156,0.45191963),
vec2(-0.23217257,0.58557273),
vec2(-0.07301355,0.17182431),
vec2(-0.16234006,0.38006379),
vec2(-0.12954241,0.23618211),
vec2(-0.24551613,0.44192904),
vec2(-0.28961686,0.35877909),
vec2(-0.38799979,0.37841954),
vec2(-0.00438757,0.00421966),
vec2(-0.10012481,0.08410484),
vec2(-0.32744945,0.21221109),
vec2(-0.19583033,0.12296323),
vec2(-0.44805491,0.13207162),
vec2(-0.53205236,0.13017592),
vec2(-0.26352644,0.04577283),
vec2(-0.11385484,0.00432264),
vec2(-0.23500119,-0.01382360),
vec2(-0.49549619,-0.05803109),
vec2(-0.40719557,-0.09301052),
vec2(-0.31135785,-0.10267038),
vec2(-0.12933326,-0.05803109),
vec2(-0.30274227,-0.14768807),
vec2(-0.21131469,-0.11457246),
vec2(-0.59592015,-0.39685018),
vec2(-0.25546340,-0.18997482),
vec2(-0.45524874,-0.39691673),
vec2(-0.07789804,-0.07213821),
vec2(-0.27761756,-0.26104338),
vec2(-0.12737260,-0.15032489),
vec2(-0.16234006,-0.21180934),
vec2(-0.32685807,-0.46619604),
vec2(-0.25036204,-0.36069107),
vec2(-0.37943402,-0.56404627),
vec2(-0.26733574,-0.43526295),
vec2(-0.13287892,-0.27997877),
vec2(-0.16750167,-0.49326106),
vec2(-0.13500824,-0.56613111),
vec2(-0.10091409,-0.57171903),
vec2(-0.05047729,-0.41289811),
vec2(-0.02064158,-0.44991499),
vec2(-0.01253636,-0.71806976),
vec2(0.03604493,-0.57442288),
vec2(0.04495378,-0.26883295),
vec2(0.09301052,-0.44528349),
vec2(0.10446969,-0.40192394),
vec2(0.19944095,-0.51345435),
vec2(0.08408924,-0.18145573),
vec2(0.33245614,-0.65107365),
vec2(0.03689907,-0.05366811),
vec2(0.15788602,-0.19194898),
vec2(0.11975302,-0.13601464),
vec2(0.28775928,-0.22691073),
vec2(0.41765860,-0.31151113),
vec2(0.06947403,-0.04470204),
vec2(0.20476059,-0.12535655),
vec2(0.41929605,-0.24706159),
vec2(0.31624585,-0.16265639),
vec2(0.12655054,-0.04959968),
vec2(0.47365805,-0.17300603),
vec2(0.41391739,-0.09563807),
vec2(0.31467647,-0.01226012));
vec4 decodePair(uvec2 words) {
    return vec4(words.x&65535u,words.x>>16u,words.y&65535u,words.y>>16u)-32768.0;
}
#ifdef BUFFERED_PROFILES
#define read0(index) profileWords(0+(index))
#else
#define read0(index) profile0[index]
#endif
#ifdef BUFFERED_PROFILES
#define read1(index) profileWords(8568+(index))
#else
#define read1(index) profile1[index]
#endif
#ifdef BUFFERED_PROFILES
#define read2(index) profileWords(21168+(index))
#else
#define read2(index) profile2[index]
#endif
#ifdef BUFFERED_PROFILES
#define read3(index) profileWords(33768+(index))
#else
#define read3(index) profile3[index]
#endif
vec4 gem_crystal_burst_skyMaterial(vec2 position, float tick) {
    if (tick>=33.0) return vec4(0);
    float spread = 1.0-.965*exp(-.23*tick);
    vec2 field = position/spread;
    int emitter = 0;
    float nearest = 1e8;
    for (int index=0; index<84; ++index) {
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
        light1[group] = sin(power*0.00009452027);
    }
    vec4 light2[6];
    for (int group=0; group<6; ++group) {
        int base = (emitter*6+group)*25;
        vec4 power = decodePair(read1(base));
        for (int channel=0; channel<24; ++channel) {
            power += light1[channel/4][channel%4]*decodePair(read1(base+1+channel));
        }
        light2[group] = sin(power*0.00006210447);
    }
    vec4 light3[6];
    for (int group=0; group<6; ++group) {
        int base = (emitter*6+group)*25;
        vec4 power = decodePair(read2(base));
        for (int channel=0; channel<24; ++channel) {
            power += light2[channel/4][channel%4]*decodePair(read2(base+1+channel));
        }
        light3[group] = sin(power*0.00005225304);
    }
    vec4 light4[1];
    for (int group=0; group<1; ++group) {
        int base = (emitter*1+group)*25;
        vec4 power = decodePair(read3(base));
        for (int channel=0; channel<24; ++channel) {
            power += light3[channel/4][channel%4]*decodePair(read3(base+1+channel));
        }
        light4[group] = (power*0.00001829665);
    }
    vec4 paint = clamp(light4[0],0.0,1.0);
    return vec4(paint.rgb,paint.a*paint.a);
}
