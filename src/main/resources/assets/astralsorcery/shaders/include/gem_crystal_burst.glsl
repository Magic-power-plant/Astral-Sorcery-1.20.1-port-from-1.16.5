#ifdef BUFFERED_PROFILES
layout(std140) uniform Profiles0 { uvec4 words0[4096]; };
layout(std140) uniform Profiles1 { uvec4 words1[4096]; };
layout(std140) uniform Profiles2 { uvec4 words2[4096]; };
layout(std140) uniform Profiles3 { uvec4 words3[4096]; };
layout(std140) uniform Profiles4 { uvec4 words4[4096]; };
layout(std140) uniform Profiles5 { uvec4 words5[443]; };
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
const vec2 launch[98] = vec2[98](vec2(0.44237621,0.00429491),
vec2(0.49323219,0.00852512),
vec2(0.09878304,0.00424342),
vec2(0.63561964,0.02912817),
vec2(0.26326362,0.02021968),
vec2(0.20228749,0.02889821),
vec2(0.45150353,0.06908496),
vec2(0.35013381,0.07254222),
vec2(0.47333150,0.14188575),
vec2(0.42412156,0.22255884),
vec2(0.36533243,0.23935573),
vec2(0.21520262,0.15028839),
vec2(0.16531347,0.11629014),
vec2(0.41289811,0.31624585),
vec2(0.31647444,0.27427785),
vec2(0.07458868,0.07044028),
vec2(0.18645956,0.23774518),
vec2(0.20458971,0.28589101),
vec2(0.28109957,0.39622285),
vec2(0.19912725,0.36371696),
vec2(0.04524395,0.10282716),
vec2(0.05661636,0.14863461),
vec2(0.06831291,0.24137226),
vec2(0.05803109,0.37050616),
vec2(0.00450906,0.17585341),
vec2(-0.02953761,0.59659937),
vec2(-0.03187936,0.33245614),
vec2(-0.08107660,0.25176419),
vec2(-0.05661636,0.15966722),
vec2(-0.27641686,0.65174277),
vec2(-0.33816231,0.62704346),
vec2(-0.21180934,0.28961686),
vec2(-0.36159980,0.39492585),
vec2(-0.06247669,0.05990227),
vec2(-0.23622031,0.21904065),
vec2(-0.42392400,0.39303257),
vec2(-0.18577423,0.16807970),
vec2(-0.31494175,0.26674012),
vec2(-0.45921182,0.32284989),
vec2(-0.27916945,0.12335586),
vec2(-0.39631450,0.16984907),
vec2(-0.56766576,0.15120817),
vec2(-0.21909235,0.01316271),
vec2(-0.29015543,-0.00438757),
vec2(-0.10282716,-0.00411309),
vec2(-0.39049366,-0.03071298),
vec2(-0.73687401,-0.10358864),
vec2(-0.16988537,-0.02953761),
vec2(-0.29669392,-0.07103939),
vec2(-0.41322871,-0.11889701),
vec2(-0.13303672,-0.03983174),
vec2(-0.24895989,-0.07458868),
vec2(-0.21799359,-0.12597847),
vec2(-0.44167268,-0.27271529),
vec2(-0.06149846,-0.04509887),
vec2(-0.34391430,-0.26326362),
vec2(-0.42858587,-0.35220423),
vec2(-0.00446393,-0.00404394),
vec2(-0.31232314,-0.29396714),
vec2(-0.10188161,-0.10188161),
vec2(-0.14284093,-0.14464875),
vec2(-0.22766041,-0.23658827),
vec2(-0.28380297,-0.37841954),
vec2(-0.31382422,-0.44816907),
vec2(-0.15807399,-0.25048065),
vec2(-0.08274698,-0.16984907),
vec2(-0.12844992,-0.32176664),
vec2(-0.17495477,-0.56965399),
vec2(-0.07764188,-0.35341359),
vec2(-0.03865423,-0.28606966),
vec2(-0.05661636,-0.43181728),
vec2(-0.02080583,-0.35094010),
vec2(-0.00419922,-0.19776513),
vec2(0.04790615,-0.42244513),
vec2(0.02330770,-0.11159824),
vec2(0.11889701,-0.40975898),
vec2(0.16698770,-0.55139854),
vec2(0.16850517,-0.46822067),
vec2(0.13217725,-0.30212822),
vec2(0.11017278,-0.17989034),
vec2(0.19451879,-0.29367698),
vec2(0.37841954,-0.51399087),
vec2(0.31947092,-0.41474329),
vec2(0.16807970,-0.21056276),
vec2(0.13673701,-0.14502410),
vec2(0.21156509,-0.20700910),
vec2(0.30921241,-0.28308179),
vec2(0.41542174,-0.34564236),
vec2(0.60109698,-0.40669003),
vec2(0.22364194,-0.14935164),
vec2(0.31555270,-0.16858295),
vec2(0.14377124,-0.07592841),
vec2(0.36721092,-0.19147429),
vec2(0.26215350,-0.06298835),
vec2(0.60713477,-0.10608561),
vec2(0.43774732,-0.06911800),
vec2(0.18197711,-0.02860695),
vec2(0.35539310,-0.04754904));
vec4 decodePair(uvec2 words) {
    return vec4(words.x&65535u,words.x>>16u,words.y&65535u,words.y>>16u)-32768.0;
}
#ifdef BUFFERED_PROFILES
#define read0(index) profileWords(0+(index))
#else
#define read0(index) profile0[index]
#endif
#ifdef BUFFERED_PROFILES
#define read1(index) profileWords(9996+(index))
#else
#define read1(index) profile1[index]
#endif
#ifdef BUFFERED_PROFILES
#define read2(index) profileWords(24696+(index))
#else
#define read2(index) profile2[index]
#endif
#ifdef BUFFERED_PROFILES
#define read3(index) profileWords(39396+(index))
#else
#define read3(index) profile3[index]
#endif
vec4 gem_crystal_burstMaterial(vec2 position, float tick) {
    if (tick>=33.0) return vec4(0);
    float spread = 1.0-.965*exp(-.23*tick);
    vec2 field = position/spread;
    int emitter = 0;
    float nearest = 1e8;
    for (int index=0; index<98; ++index) {
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
        light1[group] = sin(power*0.00012799723);
    }
    vec4 light2[6];
    for (int group=0; group<6; ++group) {
        int base = (emitter*6+group)*25;
        vec4 power = decodePair(read1(base));
        for (int channel=0; channel<24; ++channel) {
            power += light1[channel/4][channel%4]*decodePair(read1(base+1+channel));
        }
        light2[group] = sin(power*0.00007063036);
    }
    vec4 light3[6];
    for (int group=0; group<6; ++group) {
        int base = (emitter*6+group)*25;
        vec4 power = decodePair(read2(base));
        for (int channel=0; channel<24; ++channel) {
            power += light2[channel/4][channel%4]*decodePair(read2(base+1+channel));
        }
        light3[group] = sin(power*0.00005007887);
    }
    vec4 light4[1];
    for (int group=0; group<1; ++group) {
        int base = (emitter*1+group)*25;
        vec4 power = decodePair(read3(base));
        for (int channel=0; channel<24; ++channel) {
            power += light3[channel/4][channel%4]*decodePair(read3(base+1+channel));
        }
        light4[group] = (power*0.00001897773);
    }
    vec4 paint = clamp(light4[0],0.0,1.0);
    return vec4(paint.rgb,paint.a*paint.a);
}
