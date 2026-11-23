#ifdef BUFFERED_PROFILES
layout(std140) uniform Profiles0 { uvec4 words0[4096]; };
layout(std140) uniform Profiles1 { uvec4 words1[4096]; };
layout(std140) uniform Profiles2 { uvec4 words2[4096]; };
layout(std140) uniform Profiles3 { uvec4 words3[4096]; };
layout(std140) uniform Profiles4 { uvec4 words4[4096]; };
layout(std140) uniform Profiles5 { uvec4 words5[4096]; };
layout(std140) uniform Profiles6 { uvec4 words6[4096]; };
layout(std140) uniform Profiles7 { uvec4 words7[4096]; };
layout(std140) uniform Profiles8 { uvec4 words8[4096]; };
layout(std140) uniform Profiles9 { uvec4 words9[4096]; };
layout(std140) uniform Profiles10 { uvec4 words10[4096]; };
layout(std140) uniform Profiles11 { uvec4 words11[3622]; };
uvec2 profileWords(int pair) {
    int index = (pair/2)%4096;
    uvec4 words;
    if (pair<8192) words = words0[index];
    else if (pair<16384) words = words1[index];
    else if (pair<24576) words = words2[index];
    else if (pair<32768) words = words3[index];
    else if (pair<40960) words = words4[index];
    else if (pair<49152) words = words5[index];
    else if (pair<57344) words = words6[index];
    else if (pair<65536) words = words7[index];
    else if (pair<73728) words = words8[index];
    else if (pair<81920) words = words9[index];
    else if (pair<90112) words = words10[index];
    else words = words11[index];
    return pair%2==0 ? words.xy : words.zw;
}
#endif
const vec2 launch[228] = vec2[228](vec2(0.16044434,-0.75526239),
vec2(0.06652570,-0.71612962),
vec2(0.01173983,-0.66917030),
vec2(-0.22305677,-0.62221098),
vec2(0.16827089,-0.62221098),
vec2(-0.06652570,-0.60655788),
vec2(0.04304604,-0.59873132),
vec2(-0.12131158,-0.56742511),
vec2(-0.27784264,-0.55959856),
vec2(-0.17609745,-0.55177200),
vec2(0.18392400,-0.50481268),
vec2(-0.09783192,-0.48915958),
vec2(0.05087260,-0.48915958),
vec2(-0.18392400,-0.48133303),
vec2(-0.54394545,-0.47350647),
vec2(-0.26218953,-0.47350647),
vec2(0.22305677,-0.45785337),
vec2(-0.18392400,-0.43437371),
vec2(0.12131158,-0.43437371),
vec2(0.26218953,-0.42654715),
vec2(0.49698613,-0.41872060),
vec2(-0.27784264,-0.38741439),
vec2(0.01956638,-0.38741439),
vec2(-0.54394545,-0.36393473),
vec2(-0.33262851,-0.35610817),
vec2(0.50481268,-0.35610817),
vec2(0.31697541,-0.34828162),
vec2(0.11348502,-0.33262851),
vec2(-0.02739294,-0.32480196),
vec2(0.36393473,-0.32480196),
vec2(-0.23870987,-0.31697541),
vec2(0.22305677,-0.31697541),
vec2(0.16827089,-0.30132230),
vec2(-0.29349575,-0.29349575),
vec2(-0.19957711,-0.29349575),
vec2(-0.07435226,-0.28566919),
vec2(0.09000536,-0.28566919),
vec2(0.55959856,-0.28566919),
vec2(-0.03521949,-0.26218953),
vec2(-0.41872060,-0.25436298),
vec2(0.05869915,-0.25436298),
vec2(-0.67699686,-0.24653643),
vec2(0.30914885,-0.24653643),
vec2(-0.34828162,-0.23088332),
vec2(-0.16044434,-0.23088332),
vec2(-0.09783192,-0.23088332),
vec2(0.11348502,-0.22305677),
vec2(0.17609745,-0.20740366),
vec2(-0.12913813,-0.19957711),
vec2(0.35610817,-0.19957711),
vec2(0.42654715,-0.16827089),
vec2(-0.29349575,-0.16044434),
vec2(0.10565847,-0.16044434),
vec2(0.26218953,-0.16044434),
vec2(-0.60655788,-0.15261779),
vec2(-0.19175055,-0.15261779),
vec2(0.35610817,-0.15261779),
vec2(-0.25436298,-0.14479124),
vec2(0.66917030,-0.14479124),
vec2(-0.43437371,-0.13696468),
vec2(-0.01956638,-0.13696468),
vec2(-0.06652570,-0.12913813),
vec2(0.29349575,-0.12913813),
vec2(0.42654715,-0.10565847),
vec2(0.61438443,-0.10565847),
vec2(-0.48915958,-0.09783192),
vec2(-0.25436298,-0.09783192),
vec2(0.00391328,-0.09000536),
vec2(0.14479124,-0.09000536),
vec2(0.35610817,-0.08217881),
vec2(-0.14479124,-0.06652570),
vec2(0.05869915,-0.06652570),
vec2(-0.02739294,-0.05869915),
vec2(0.49698613,-0.05869915),
vec2(0.26218953,-0.05087260),
vec2(-0.37958783,-0.04304604),
vec2(0.11348502,-0.04304604),
vec2(-0.21523021,-0.03521949),
vec2(-0.55959856,-0.02739294),
vec2(-0.42654715,-0.02739294),
vec2(-0.09783192,-0.02739294),
vec2(0.18392400,-0.02739294),
vec2(0.43437371,-0.01956638),
vec2(0.48133303,-0.01956638),
vec2(-0.28566919,-0.01173983),
vec2(0.30914885,-0.00391328),
vec2(-0.47350647,0.00391328),
vec2(0.39524094,0.00391328),
vec2(-0.07435226,0.01173983),
vec2(0.06652570,0.01173983),
vec2(0.21523021,0.01956638),
vec2(-0.58307822,0.02739294),
vec2(0.54394545,0.02739294),
vec2(-0.22305677,0.03521949),
vec2(0.12131158,0.03521949),
vec2(-0.48133303,0.05087260),
vec2(-0.26218953,0.05087260),
vec2(0.00391328,0.05869915),
vec2(0.41089405,0.05869915),
vec2(-0.04304604,0.06652570),
vec2(0.06652570,0.06652570),
vec2(0.27784264,0.06652570),
vec2(-0.11348502,0.07435226),
vec2(-0.16827089,0.08217881),
vec2(-0.36393473,0.09000536),
vec2(0.47350647,0.09783192),
vec2(-0.42654715,0.11348502),
vec2(-0.30914885,0.11348502),
vec2(-0.02739294,0.11348502),
vec2(0.03521949,0.11348502),
vec2(0.19957711,0.11348502),
vec2(0.09000536,0.12131158),
vec2(-0.62221098,0.12913813),
vec2(0.36393473,0.12913813),
vec2(-0.77091550,0.13696468),
vec2(0.30132230,0.13696468),
vec2(-0.36393473,0.15261779),
vec2(-0.01173983,0.15261779),
vec2(0.22305677,0.15261779),
vec2(-0.24653643,0.16044434),
vec2(-0.07435226,0.16044434),
vec2(0.11348502,0.16044434),
vec2(-0.13696468,0.16827089),
vec2(0.26218953,0.16827089),
vec2(0.33262851,0.16827089),
vec2(0.57525166,0.16827089),
vec2(-0.61438443,0.17609745),
vec2(-0.19175055,0.17609745),
vec2(0.48915958,0.17609745),
vec2(-0.34828162,0.19957711),
vec2(-0.16044434,0.21523021),
vec2(0.19175055,0.21523021),
vec2(-0.66917030,0.23088332),
vec2(0.03521949,0.23088332),
vec2(-0.05087260,0.26218953),
vec2(0.52829234,0.26218953),
vec2(-0.09783192,0.27001609),
vec2(-0.18392400,0.28566919),
vec2(0.01956638,0.28566919),
vec2(0.23088332,0.28566919),
vec2(-0.26218953,0.30132230),
vec2(-0.34828162,0.31697541),
vec2(0.53611890,0.33262851),
vec2(-0.05869915,0.34045507),
vec2(0.05087260,0.34045507),
vec2(0.13696468,0.34045507),
vec2(-0.15261779,0.35610817),
vec2(0.31697541,0.36393473),
vec2(-0.47350647,0.37176128),
vec2(-0.30132230,0.37176128),
vec2(-0.01956638,0.37176128),
vec2(0.48133303,0.38741439),
vec2(-0.06652570,0.40306749),
vec2(0.25436298,0.40306749),
vec2(0.30914885,0.41872060),
vec2(-0.50481268,0.42654715),
vec2(0.09000536,0.42654715),
vec2(-0.09783192,0.44220026),
vec2(-0.01173983,0.44220026),
vec2(0.20740366,0.45002681),
vec2(-0.22305677,0.46567992),
vec2(-0.05087260,0.46567992),
vec2(0.26218953,0.47350647),
vec2(0.12131158,0.48915958),
vec2(0.37958783,0.48915958),
vec2(0.00391328,0.51263924),
vec2(0.28566919,0.52829234),
vec2(0.14479124,0.53611890),
vec2(-0.15261779,0.54394545),
vec2(0.06652570,0.55177200),
vec2(0.24653643,0.57525166),
vec2(-0.07435226,0.62221098),
vec2(-0.02908734,-0.42799936),
vec2(0.05401934,-0.42799936),
vec2(-0.23685402,-0.40306736),
vec2(-0.10388334,-0.35320336),
vec2(0.46124203,-0.34489269),
vec2(0.22854335,-0.24516468),
vec2(0.07895134,-0.19530068),
vec2(0.08726201,-0.10388334),
vec2(0.22854335,-0.02077667),
vec2(0.36151402,0.04570867),
vec2(0.15374734,0.06233000),
vec2(0.26178602,0.11219401),
vec2(-0.16205801,0.12881534),
vec2(-0.07895134,0.21192201),
vec2(0.31165002,0.22023268),
vec2(-0.38644603,0.24516468),
vec2(0.06233000,0.27840735),
vec2(0.29502869,0.31996069),
vec2(-0.38644603,0.33658202),
vec2(-0.10388334,0.34489269),
vec2(-0.07064067,0.50279537),
vec2(0.05438957,-0.65761938),
vec2(-0.03461155,-0.48950615),
vec2(0.29172589,-0.46972813),
vec2(-0.50928418,-0.44006109),
vec2(-0.32139293,-0.44006109),
vec2(-0.11372365,-0.41039405),
vec2(-0.37083799,-0.39061602),
vec2(0.15327970,-0.37083799),
vec2(0.21261378,-0.36094898),
vec2(-0.47961714,-0.29172589),
vec2(-0.12361266,-0.28183688),
vec2(0.26205885,-0.28183688),
vec2(-0.04450056,-0.20272477),
vec2(-0.22250280,-0.18294674),
vec2(-0.38072701,-0.16316872),
vec2(0.02472253,-0.16316872),
vec2(-0.30161490,-0.11372365),
vec2(-0.18294674,-0.09394563),
vec2(-0.50928418,-0.03461155),
vec2(-0.31150392,0.02472253),
vec2(0.53895122,0.11372365),
vec2(-0.71695346,0.12361266),
vec2(0.41039405,0.14339069),
vec2(-0.22250280,0.21261378),
vec2(-0.30161490,0.22250280),
vec2(-0.14339069,0.26205885),
vec2(0.36094898,0.26205885),
vec2(-0.21261378,0.32139293),
vec2(0.05438957,0.39061602),
vec2(-0.12361266,0.40050503),
vec2(-0.19283576,0.41039405),
vec2(-0.35105997,0.43017207),
vec2(0.14339069,0.44006109),
vec2(0.22250280,0.52906221),
vec2(0.07416760,0.59828530));
vec4 decodePair(uvec2 words) {
    return vec4(words.x&65535u,words.x>>16u,words.y&65535u,words.y>>16u)-32768.0;
}
#ifdef BUFFERED_PROFILES
#define read0(index) profileWords(0+(index))
#else
#define read0(index) profile0[index]
#endif
#ifdef BUFFERED_PROFILES
#define read1(index) profileWords(23256+(index))
#else
#define read1(index) profile1[index]
#endif
#ifdef BUFFERED_PROFILES
#define read2(index) profileWords(57456+(index))
#else
#define read2(index) profile2[index]
#endif
#ifdef BUFFERED_PROFILES
#define read3(index) profileWords(91656+(index))
#else
#define read3(index) profile3[index]
#endif
vec4 collector_crystal_burstMaterial(vec2 position, float tick) {
    if (tick>=80.0) return vec4(0);
    float spread = 1.0-.86*pow(max(0.0,1.0-tick/59.0),2.7);
    vec2 field = position/spread;
    int emitter = 0;
    float nearest = 1e8;
    for (int index=0; index<228; ++index) {
        vec2 displacement = field-launch[index];
        float squared = dot(displacement,displacement);
        if (squared<nearest) { nearest=squared; emitter=index; }
    }
    vec2 axis = normalize(launch[emitter]+vec2(1e-10,0.0));
    vec2 delta = (position-launch[emitter]*spread)*80.0;
    vec2 offset = vec2(dot(delta,axis),dot(delta,vec2(-axis.y,axis.x)));
    offset = sign(offset)*log(abs(offset)+sqrt(1.0+offset*offset));
    float age = tick/79.0;
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
        light1[group] = sin(power*0.00006472967);
    }
    vec4 light2[6];
    for (int group=0; group<6; ++group) {
        int base = (emitter*6+group)*25;
        vec4 power = decodePair(read1(base));
        for (int channel=0; channel<24; ++channel) {
            power += light1[channel/4][channel%4]*decodePair(read1(base+1+channel));
        }
        light2[group] = sin(power*0.00005295242);
    }
    vec4 light3[6];
    for (int group=0; group<6; ++group) {
        int base = (emitter*6+group)*25;
        vec4 power = decodePair(read2(base));
        for (int channel=0; channel<24; ++channel) {
            power += light2[channel/4][channel%4]*decodePair(read2(base+1+channel));
        }
        light3[group] = sin(power*0.00005054933);
    }
    vec4 light4[1];
    for (int group=0; group<1; ++group) {
        int base = (emitter*1+group)*25;
        vec4 power = decodePair(read3(base));
        for (int channel=0; channel<24; ++channel) {
            power += light3[channel/4][channel%4]*decodePair(read3(base+1+channel));
        }
        light4[group] = (power*0.00001729486);
    }
    vec4 paint = clamp(light4[0],0.0,1.0);
    return vec4(paint.rgb,paint.a*paint.a);
}
