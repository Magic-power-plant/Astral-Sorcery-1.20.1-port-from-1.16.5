#ifdef BUFFERED_PROFILES
layout(std140) uniform Profiles0 { uvec4 words0[4096]; };
layout(std140) uniform Profiles1 { uvec4 words1[4096]; };
layout(std140) uniform Profiles2 { uvec4 words2[4096]; };
layout(std140) uniform Profiles3 { uvec4 words3[4096]; };
layout(std140) uniform Profiles4 { uvec4 words4[4096]; };
layout(std140) uniform Profiles5 { uvec4 words5[1084]; };
uvec2 profileWords(int pair) {
    int index = (pair/2)%4096;
    uvec4 words;
    if (pair<8192) words = words0[index];
    else if (pair<16384) words = words1[index];
    else if (pair<24576) words = words2[index];
    else if (pair<32768) words = words3[index];
    else if (pair<40960) words = words4[index];
    else words = words5[index];
    return pair%2==0 ? words.xy : words.zw;
}
#endif
const vec2 launch[101] = vec2[101](vec2(0.44418819,0.01306531),
vec2(0.51922534,0.09145719),
vec2(0.39049366,0.07621970),
vec2(0.11337903,0.02912817),
vec2(0.20980470,0.07588680),
vec2(0.58355636,0.21960792),
vec2(0.58819754,0.25332531),
vec2(0.61625014,0.26908171),
vec2(0.06859984,0.03631756),
vec2(0.15129239,0.09077543),
vec2(0.03156343,0.03098025),
vec2(0.26285582,0.26285582),
vec2(0.35468684,0.37087583),
vec2(0.17572842,0.20087684),
vec2(0.33245614,0.46252244),
vec2(0.06947403,0.10216769),
vec2(0.20202045,0.36506775),
vec2(0.12668793,0.25602075),
vec2(0.14004675,0.31279861),
vec2(0.18895863,0.49955561),
vec2(0.09261258,0.28961686),
vec2(0.10012481,0.32440438),
vec2(0.05366811,0.18132194),
vec2(0.11064374,0.38297329),
vec2(0.06947403,0.24052057),
vec2(0.08336381,0.48702018),
vec2(0.02303933,0.31758873),
vec2(-0.01248350,0.40944174),
vec2(-0.03225507,0.22211402),
vec2(-0.03779301,0.19095410),
vec2(-0.07833373,0.30824441),
vec2(-0.12797780,0.24081816),
vec2(-0.19915872,0.37172477),
vec2(-0.37657434,0.57589989),
vec2(-0.17534160,0.25112242),
vec2(-0.06874691,0.08582086),
vec2(-0.01999211,0.01988469),
vec2(-0.43048919,0.40637475),
vec2(-0.37034383,0.33705450),
vec2(-0.36045603,0.32395415),
vec2(-0.27047583,0.17789301),
vec2(-0.55105743,0.34961546),
vec2(-0.60263051,0.33564666),
vec2(-0.34961546,0.16850517),
vec2(-0.32298231,0.10608561),
vec2(-0.54723173,0.16486605),
vec2(-0.14478978,0.01999211),
vec2(-0.50574884,0.06483960),
vec2(-0.42354001,0.05009613),
vec2(-0.34275749,0.03689907),
vec2(-0.61738831,-0.06130061),
vec2(-0.65707878,-0.07586107),
vec2(-0.40532324,-0.04693909),
vec2(-0.07173421,-0.01280157),
vec2(-0.64161307,-0.16858295),
vec2(-0.33816231,-0.09634511),
vec2(-0.22146487,-0.06845278),
vec2(-0.62102729,-0.27231300),
vec2(-0.52554732,-0.30404089),
vec2(-0.30272465,-0.18548120),
vec2(-0.39796325,-0.26331773),
vec2(-0.22996215,-0.16640981),
vec2(-0.08160338,-0.07301355),
vec2(-0.23618211,-0.22766041),
vec2(-0.45444340,-0.53990325),
vec2(-0.04253247,-0.05990227),
vec2(-0.13731850,-0.21405453),
vec2(-0.17152367,-0.28554685),
vec2(-0.19463272,-0.44931690),
vec2(-0.09563807,-0.38710646),
vec2(-0.02212875,-0.10764070),
vec2(-0.04253247,-0.33173972),
vec2(-0.02043354,-0.69762350),
vec2(-0.00417879,-0.35519694),
vec2(0.01288474,-0.24010578),
vec2(0.00833995,-0.09946964),
vec2(0.06825474,-0.69753537),
vec2(0.07636287,-0.50755089),
vec2(0.05127693,-0.31947092),
vec2(0.10895269,-0.47680594),
vec2(0.11951627,-0.38435196),
vec2(0.23658827,-0.67848315),
vec2(0.08701446,-0.17247696),
vec2(0.16113886,-0.28605310),
vec2(0.37793540,-0.59220163),
vec2(0.04497299,-0.06638624),
vec2(0.02177552,-0.03098025),
vec2(0.28554685,-0.34613016),
vec2(0.11848944,-0.13637583),
vec2(0.19730506,-0.21341160),
vec2(0.26883295,-0.21499089),
vec2(0.20024866,-0.13673701),
vec2(0.40538302,-0.23471592),
vec2(0.28701682,-0.13144053),
vec2(0.14635609,-0.05409517),
vec2(0.37606965,-0.10216769),
vec2(0.59659937,-0.15939678),
vec2(0.52229092,-0.13344988),
vec2(0.65388476,-0.13924875),
vec2(0.53058817,-0.07103939),
vec2(0.13207162,-0.00466154));
vec4 decodePair(uvec2 words) {
    return vec4(words.x&65535u,words.x>>16u,words.y&65535u,words.y>>16u)-32768.0;
}
#ifdef BUFFERED_PROFILES
#define read0(index) profileWords(0+(index))
#else
#define read0(index) profile0[index]
#endif
#ifdef BUFFERED_PROFILES
#define read1(index) profileWords(10302+(index))
#else
#define read1(index) profile1[index]
#endif
#ifdef BUFFERED_PROFILES
#define read2(index) profileWords(25452+(index))
#else
#define read2(index) profile2[index]
#endif
#ifdef BUFFERED_PROFILES
#define read3(index) profileWords(40602+(index))
#else
#define read3(index) profile3[index]
#endif
vec4 gem_crystal_burst_nightMaterial(vec2 position, float tick) {
    if (tick>=33.0) return vec4(0);
    float spread = 1.0-.965*exp(-.23*tick);
    vec2 field = position/spread;
    int emitter = 0;
    float nearest = 1e8;
    for (int index=0; index<101; ++index) {
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
        light1[group] = sin(power*0.00010045997);
    }
    vec4 light2[6];
    for (int group=0; group<6; ++group) {
        int base = (emitter*6+group)*25;
        vec4 power = decodePair(read1(base));
        for (int channel=0; channel<24; ++channel) {
            power += light1[channel/4][channel%4]*decodePair(read1(base+1+channel));
        }
        light2[group] = sin(power*0.00006815533);
    }
    vec4 light3[6];
    for (int group=0; group<6; ++group) {
        int base = (emitter*6+group)*25;
        vec4 power = decodePair(read2(base));
        for (int channel=0; channel<24; ++channel) {
            power += light2[channel/4][channel%4]*decodePair(read2(base+1+channel));
        }
        light3[group] = sin(power*0.00004737341);
    }
    vec4 light4[1];
    for (int group=0; group<1; ++group) {
        int base = (emitter*1+group)*25;
        vec4 power = decodePair(read3(base));
        for (int channel=0; channel<24; ++channel) {
            power += light3[channel/4][channel%4]*decodePair(read3(base+1+channel));
        }
        light4[group] = (power*0.00001383344);
    }
    vec4 paint = clamp(light4[0],0.0,1.0);
    return vec4(paint.rgb,paint.a*paint.a);
}
