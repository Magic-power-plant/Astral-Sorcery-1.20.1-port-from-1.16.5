#version 150

uniform vec4 Effect;
uniform vec3 Palette;
uniform vec4 ColorModulator;
in vec2 sheetUV;
in vec4 tint;
out vec4 fragColor;

const float TAU = 6.28318530718;

float hash(vec2 key) {
    vec3 mixed = fract(key.xyx * vec3(0.1031, 0.1030, 0.0973));
    mixed += dot(mixed, mixed.yzx + 33.33);
    return fract((mixed.x + mixed.y) * mixed.z);
}

float valueNoise(vec2 position) {
    vec2 cell = floor(position), part = fract(position);
    part *= part * (3.0 - 2.0 * part);
    return mix(mix(hash(cell), hash(cell + vec2(1, 0)), part.x),
               mix(hash(cell + vec2(0, 1)), hash(cell + vec2(1, 1)), part.x), part.y);
}

float cloud(vec2 position) {
    return 0.57 * valueNoise(position) + 0.29 * valueNoise(position * 2.07 + 3.0) + 0.14 * valueNoise(position * 4.17 + 7.0);
}

vec2 direction(float angle) { return vec2(cos(angle), sin(angle)); }
float gaussian(float value) { return exp(-value * value); }
float bell(float value, float center, float width) { return gaussian((value-center)/width); }
vec4 glow(float energy, vec3 base) {
    return vec4(mix(base, vec3(1), smoothstep(0.45, 1.9, energy)), clamp(energy, 0.0, 1.0));
}

float spokes(float angle, float count, float phase, float sharpness) {
    return pow(clamp(0.5 + 0.5*cos(count*angle + phase),0.0,1.0), sharpness);
}

vec4 flare(vec2 pos, float radius, float angle, float phase, int kind) {
    vec2 orbit = direction(phase);
    vec2 polar = direction(angle + 0.5*radius*sin(phase)) * (0.5 + radius);
    float gas = cloud(polar * 6.2 + orbit * 0.9);
    float blades = spokes(angle + 0.8*radius, 7.0, phase, 4.0)
                 + 0.8*spokes(angle - 0.6*radius, 11.0, -phase, 9.0);
    if (kind == 0) {
        float aura = (0.4+1.1*gas)*bell(radius, 0.43, 0.12);
        float stars = pow(valueNoise(pos * 27.0 + orbit*0.8), 9.0)*4.8;
        float core = gaussian(radius/0.20)*(1.1 + 1.7*gas);
        float energy = core + aura*(0.4+gas) + stars*gaussian(radius/0.48);
        return glow(energy, mix(vec3(0.39,0.32,1),Palette,smoothstep(0.17,0.42,radius)));
    }
    if (kind == 3) {
        float curls = 0.3 + cloud(pos*9.0 + 1.3*polar + orbit)*0.9;
        float energy = (0.1+2.0*pow(curls, 3.0))*gaussian(radius/0.36)
                     + 1.9*gaussian(radius/0.12) + 0.6*blades*gaussian(radius/0.36);
        return vec4(min(energy,1.0));
    }
    if (kind == 10) {
        float energy = bell(radius,0.41+0.045*sin(angle*7.0+phase),0.12) * (0.18+1.1*gas+0.8*blades);
        return glow(energy,Palette*0.85);
    }
    return vec4(0);
}

vec4 rimUnpack(uint packedRadii) {
    return vec4(uvec4(packedRadii,packedRadii>>8u,packedRadii>>16u,packedRadii>>24u)&uvec4(255u))-128.0;
}

#moj_import <astralsorcery:dazzling_aura.glsl>
#moj_import <astralsorcery:halo_infusion.glsl>
#moj_import <astralsorcery:halo_ritual.glsl>
#moj_import <astralsorcery:craft_front.glsl>

float rimDepth(float radius, vec4 rims) {
    return min((radius-rims.x)/max(.008,rims.z-rims.x),(rims.y-radius)/max(.008,rims.y-rims.w));
}

vec4 ring(vec2 pos, float radius, float angle, float time, int kind) {
    float phase = TAU*time;
    vec2 orbit = direction(phase);
    if (kind == 2) {
        float tick = time*48.0;
        if (tick>=42.0) return vec4(0);
        float center = -.08632+.01125*tick+.28109*(1.0-exp(-.12821*tick));
        float width = .06224+.10316*exp(-.16759*tick);
        float scale = 5.6761*exp(-.059806*tick)/(1.0+exp((tick-36.9555)*.52489));
        scale = mix(scale,.0626*pow(max(0.0,41.60-tick),1.12),smoothstep(30.0,37.0,tick));
        vec4 front = shockFront(angle/TAU,direction(tick*TAU/84.0));
        float distance = radius-center-.06*front.x;
        float aperture = width*exp(clamp(distance<0.0 ? front.y : front.z,-2.0,2.0));
        float energy = scale*exp(clamp(front.w,-4.0,4.0)-pow(abs(distance)/aperture,2.2983));
        if (tick>34.0) {
            float across = mod(degrees(angle)-297.75+180.0,360.0)-180.0;
            float jet = 1.74*gaussian(across/2.1);
            float wave = .26*cos(2.0*angle+2.04)+.19*cos(9.0*angle-2.24)+.13*cos(26.0*angle+.91);
            float roughness = valueNoise(direction(angle)*32.0+orbit)-.5;
            float residual = scale*exp(-pow(abs(radius-center-.02*roughness)/width,2.2983))*(1.0+wave+jet);
            energy = max(energy,residual*smoothstep(.2,.9,jet)*smoothstep(34.0,40.0,tick));
        }
        float pigment = clamp(shockOptics[8]+shockOptics[9]*tick,0.0,.92);
        float hot = (1.0-exp(-shockOptics[3]*pow(max(.00001,energy-shockOptics[4]),shockOptics[5])))*(1.0-exp(-tick/shockOptics[6]));
        float red = mix(pigment,1.0,min(1.0,hot+gaussian(radius/shockOptics[7])*exp(-.42*tick)));
        float blue = min(1.0,mix(shockOptics[0],1.0,pigment)+shockOptics[1]*smoothstep(shockOptics[2],shockOptics[2]+.93,energy));
        return vec4(red,red,blue,min(energy,1.0));
    }
    float azimuth = angle/TAU;
    if (radius<.22 || radius>.99) return vec4(0);
    vec4 rims = kind==5 ? dazzlingRims(azimuth,orbit) : kind==11 ? infusionRims(azimuth,orbit) : ritualRims(azimuth,orbit);
    float depth = rimDepth(radius,rims);
    if (depth<0.0) return vec4(0);
    float opacity = min(1.0,.1*exp(1.806*depth));
    if (kind==5) {
        float white = smoothstep(1.35,1.75,depth);
        return vec4(vec2(.703+.297*white),1.0,opacity);
    }
    if (kind==11) {
        vec4 pigment = infusionFilaments(azimuth,orbit);
        vec2 warmth = min(vec2(radius)-pigment.xz,pigment.yw-vec2(radius));
        float hot = smoothstep(-.025,.026,warmth.x);
        float red = .319+.681*hot;
        float green = .11+.32*smoothstep(-.042,.025,warmth.x)+.57*smoothstep(-.037,.035,warmth.y);
        float outer = smoothstep(.60,.74,radius);
        vec4 modes = vec4((1.0-hot)*vec2(1.0-outer,outer),bell(radius,.645,.038),bell(radius,.722,.050));
        return clamp(vec4(red+dot(infusionRed(azimuth,orbit),modes),green+dot(infusionGreen(azimuth,orbit),modes),
                          .63+.37*smoothstep(-.036,.002,warmth.x)+dot(infusionBlue(azimuth,orbit),modes),opacity),0.0,1.0);
    }
    vec4 shoulder = ritualShoulders(azimuth,orbit);
    float midDepth = rimDepth(radius,vec4(rims.xy,shoulder.xy));
    float brightDepth = rimDepth(radius,vec4(shoulder.xy,rims.zw));
    float coreDepth = rimDepth(radius,vec4(rims.zw,shoulder.zw));
    opacity = midDepth<1.0 ? .1*exp(.936*midDepth) : brightDepth<1.0 ? .255*exp(.869*brightDepth) : min(1.0,.608+.271*coreDepth);
    float distance = (2.0*radius-rims.z-rims.w)/max(.025,rims.w-rims.z);
    float red = .022+.71*smoothstep(.337,.506,radius)+.11*bell(distance,0.0,.43)+.24*smoothstep(.599,.704,radius);
    vec4 modes = vec4(bell(radius,.395,.065),bell(radius,.470,.055),bell(radius,.532,.040),bell(radius,.645,.064));
    red += dot(ritualLight(azimuth,orbit),modes);
    return clamp(vec4(red,.44+.56*red+.06*bell(distance,0.0,.50),1.0,opacity),0.0,1.0);
}

vec4 fountain(vec2 pos, float radius, float angle, float phase, bool vortex) {
    vec2 orbit = direction(phase);
    float grain = cloud(pos*19.0+orbit*1.5);
    float spiral = angle + 3.0*radius - phase;
    float energy;
    if (vortex) {
        if (radius>1.04) return vec4(0);
        float square = radius*radius;
        float twist = radians(-26.0+square*(90.0-18.0*square)+36.0*pow(radius,12.0));
        float armAngle = mod(angle-phase*(2.0/3.0)-twist+TAU/6.0,TAU/3.0)-TAU/6.0;
        float width = .221+.014/max(radius,.025);
        float arm = 1.45*gaussian(armAngle/width) + .24*bell(armAngle,width,width);
        float ringNoise = grain-.5;
        float annulus = smoothstep(.382,.447,radius+.044*ringNoise)*(1.0-smoothstep(.747,.829,radius+.11*ringNoise));
        float turbulence = clamp(.87+1.8*ringNoise-.31*bell(radius,.55,.017),0.0,1.0);
        float cap = 1.0-smoothstep(.965,1.015,radius+.033*ringNoise);
        float alpha = max(arm*cap,annulus*turbulence);
        float white = smoothstep(.50,1.12,arm*cap + annulus*(.28+1.4*ringNoise));
        return vec4(mix(Palette,vec3(1),white),min(1.0,alpha));
    } else {
        float spray = cloud(direction(spiral)*radius*8.0+orbit);
        float swells = 0.5 + 0.5*sin(28.0*radius + 10.0*spray-phase);
        energy = 1.85*gaussian(radius/0.31) + bell(radius,0.43,0.26)*(0.3 + swells + 2.4*spray*grain)
               + 1.4*bell(radius,0.66,0.21)*pow(grain*1.4,3.0);
    }
    return glow(energy,Palette*(0.65+0.6*grain));
}

const vec4 crystalA[19] = vec4[19](
    vec4(.6559,.2294,.0321,.0176),vec4(.7138,.3459,.0273,.0238),vec4(.1773,.3477,.0063,.0266),vec4(.1306,.3383,.0064,.0306),vec4(-.1332,.6996,-.0086,.0431),
    vec4(-.0190,.0194,-.0054,.0069),vec4(-.6444,.2775,-.0264,.0266),vec4(-.1825,-.0537,-.0004,.0030),vec4(-.6812,-.3941,-.0370,-.0079),vec4(-.5471,-.3389,-.0238,-.0056),
    vec4(-.2963,-.5255,-.0207,-.0143),vec4(-.4088,-.9331,-.0156,-.0313),vec4(-.0952,-.2588,-.0007,.0017),vec4(.0703,-.2963,.0028,.0120),vec4(.0987,-.3355,-.0003,.0011),
    vec4(.1135,-.1529,.0045,.0008),vec4(.4879,-.5542,.0239,-.0096),vec4(.1685,-.1281,.0082,.0067),vec4(.7112,-.2477,.0391,-.0002));
const vec4 crystalB[19] = vec4[19](
    vec4(.7289,.2555,.0369,.0256),vec4(.0519,.0175,-.0037,.0033),vec4(.0463,.0598,-.0017,.0115),vec4(.2356,.4435,.0151,.0292),vec4(.0991,.2597,.0025,.0227),
    vec4(-.0196,.2965,-.0023,.0145),vec4(-.5131,.4283,-.0263,.0274),vec4(-.2410,.1864,-.0079,.0153),vec4(-.0388,.0202,.0025,-.0014),vec4(-.3814,.0629,-.0114,.0129),
    vec4(-.8979,-.1791,-.0512,.0098),vec4(-.6135,-.5303,-.0282,-.0217),vec4(-.3781,-.4263,-.0198,-.0096),vec4(-.0177,-.0499,.0015,.0085),vec4(-.1094,-.6565,-.0076,-.0167),
    vec4(.1667,-.1923,.0020,.0048),vec4(.2597,-.2443,.0089,.0038),vec4(.1408,-.0878,.0041,.0058),vec4(.9242,-.4735,.0487,-.0132));
const vec4 crystalC[19] = vec4[19](
    vec4(.7583,.1660,.0346,.0192),vec4(.7733,.3636,.0220,.0366),vec4(.7088,.7497,.0342,.0535),vec4(.4226,.5705,.0187,.0461),vec4(.2379,.5962,.0124,.0456),
    vec4(-.1055,.6722,-.0088,.0464),vec4(-.1073,.0973,-.0061,.0075),vec4(-.6889,.2616,-.0317,.0273),vec4(-.3578,-.0804,-.0090,.0063),vec4(-.0170,-.0249,.0006,.0066),
    vec4(-.1810,-.3155,-.0016,.0068),vec4(-.4751,-.8671,-.0227,-.0370),vec4(-.0661,-.2921,-.0049,-.0021),vec4(.0346,-.1636,-.0023,.0094),vec4(.1923,-.6622,.0105,-.0102),
    vec4(.3081,-.8677,.0149,-.0335),vec4(.7078,-.7466,.0349,-.0252),vec4(.3388,-.3435,.0195,-.0044),vec4(.8319,-.6047,.0324,-.0132));

const vec2 lampA[19] = vec2[19](vec2(-.884,1.326),vec2(-.933,1.308),vec2(-.840,.931),vec2(-.961,.809),vec2(-.982,1.901),vec2(-.664,1.455),vec2(-.984,1.568),vec2(-.766,1.251),vec2(-.847,.402),vec2(-.256,.585),vec2(-.862,.758),vec2(-.798,1.588),vec2(-.812,1.876),vec2(-.807,1.127),vec2(-.750,1.448),vec2(-.879,2.081),vec2(-.764,.908),vec2(-.788,1.463),vec2(-.835,1.619));
const vec2 lampB[19] = vec2[19](vec2(-.843,1.080),vec2(-.683,.938),vec2(-.596,.071),vec2(-.896,1.865),vec2(-.809,1.136),vec2(-.789,1.020),vec2(-.946,1.430),vec2(-.736,1.267),vec2(-.080,-.054),vec2(-.912,1.710),vec2(-.784,1.832),vec2(-.762,1.271),vec2(-.850,1.988),vec2(-.571,1.028),vec2(-.831,1.859),vec2(-.716,1.624),vec2(-.685,1.442),vec2(-.724,1.449),vec2(-.803,1.200));
const vec2 lampC[19] = vec2[19](vec2(-.917,1.303),vec2(-.913,1.451),vec2(-.913,1.268),vec2(-.910,1.763),vec2(-.930,1.266),vec2(-.965,1.581),vec2(-.692,1.509),vec2(-.908,1.560),vec2(-.788,1.734),vec2(-.786,1.004),vec2(-.746,1.622),vec2(-.872,1.601),vec2(-.745,1.942),vec2(-.875,1.285),vec2(-.902,1.509),vec2(-.770,1.505),vec2(-.760,1.002),vec2(-.910,1.544),vec2(-.645,1.140));

vec4 crystalFlash(vec2 pos, float tick, float variant) {
    vec4 widths = variant<1.5 ? vec4(.11889,.00685,.06644,.14417) : variant<2.5 ? vec4(.11722,.00721,.06034,.13777) : vec4(.11688,.00924,.08932,.14688);
    vec3 lengths = variant<1.5 ? vec3(.08525,.09536,.001) : variant<2.5 ? vec3(.08921,.10078,.001) : vec3(.07134,.11363,.01641);
    float growth = (1.0-exp(-widths.x*tick)) / (1.0-exp(-widths.x*31.0));
    float bend = sin(tick*TAU/62.0);
    float width = widths.y+widths.z*exp(-widths.w*tick);
    float energy = 2.7*gaussian(length(pos)/(variant==2.0 ? .05064 : .04))*exp(-.53*tick);
    for (int index=0; index<19; ++index) {
        vec4 emitter = variant<1.5 ? crystalA[index] : variant<2.5 ? crystalB[index] : crystalC[index];
        vec2 axis = normalize(emitter.xy), delta = pos-growth*emitter.xy-bend*emitter.zw;
        float along = dot(delta,axis), across = dot(delta,vec2(-axis.y,axis.x));
        vec2 lamp = variant<1.5 ? lampA[index] : variant<2.5 ? lampB[index] : lampC[index];
        float stretch = width + length(emitter.xy)*(lengths.x*exp(-lengths.y*tick)+lengths.z*(1.0-tick/32.0));
        float head = gaussian(along/stretch)*gaussian(across/width);
        float tail = bell(along,-1.65*stretch,1.5*stretch)*gaussian(across/(.71*width));
        float fade = pow(clamp((32.097+lamp.y-tick)/8.0,0.0,1.0),variant>2.5 ? 1.398 : 1.414);
        energy += (head+.36*tail)*1.317*exp(lamp.x)*fade;
    }
    vec3 pigment = mix(vec3(0,.495,1),vec3(.29,.32,1),smoothstep(12.0,24.0,tick));
    pigment = mix(pigment,vec3(.76,.76,1),smoothstep(24.0,32.0,tick));
    pigment.g += (1.0-smoothstep(4.0,15.0,tick))*.49*smoothstep(.77,2.4,energy);
    return vec4(pigment,min(energy,1.0));
}

#moj_import <astralsorcery:crystal_embers.glsl>

vec4 crystalBurst(vec2 pos, float time, float variant) {
    float tick = time*40.0;
    if (tick>=32.0) return vec4(0);
    if (tick<=18.0) return crystalFlash(pos,tick,variant);
    vec2 point = pos*128.0+127.5;
    float drift = (1.0-exp(-.065*(tick-28.0)))/.065;
    vec2 spread = exp(-vec2(.058,.104)*(tick-28.0));
    float energy = 0.0;
    for (int index=0; index<20; ++index) {
        if (index==19 && variant<2.5) break;
        vec4 flight = variant<1.5 ? emberFlight1[index] : variant<2.5 ? emberFlight2[index] : emberFlight3[index];
        vec4 cooling = variant<1.5 ? emberCooling1[index] : variant<2.5 ? emberCooling2[index] : emberCooling3[index];
        vec4 spark = variant<1.5 ? emberSpark1[index] : variant<2.5 ? emberSpark2[index] : emberSpark3[index];
        vec2 delta = point-flight.xy-drift*flight.zw;
        vec2 axis = normalize(flight.xy-127.5);
        vec2 breadth = clamp(cooling.yz,vec2(.4,.45),vec2(3,4.5))*spread;
        float across = dot(delta,vec2(axis.y,-axis.x))/breadth.x, along = dot(delta,axis)/breadth.y;
        float lamp = clamp(cooling.x,.12,2.5)*pow(clamp((cooling.w-tick)/5.0,0.0,4.0),1.55);
        vec2 trail = vec2(across/.78,(along+1.6)/1.8);
        energy += lamp*(exp(-across*across-along*along)+.25*exp(-dot(trail,trail)));
        vec2 offset = (delta-spark.xy)/spark.w;
        energy += spark.z*clamp((32.3-tick)/7.0,0.0,2.0)*exp(-dot(offset,offset));
    }
    float dye = .27+.066*max(tick-24.0,0.0);
    vec4 ember = vec4(dye,dye+.02+.17*clamp((24.0-tick)/6.0,0.0,1.0),1.0,min(1.0,energy));
    if (tick>=22.0) return ember;
    return mix(crystalFlash(pos,tick,variant),ember,smoothstep(18.0,22.0,tick));
}

const vec2 collectorFlight[80] = vec2[80](
    vec2(-.2852,-.0117),vec2(-.5586,-.0273),vec2(-.4883,-.0977),vec2(-.2539,-.0586),vec2(-.6055,-.1523),
    vec2(-.6836,-.2461),vec2(-.1445,-.0664),vec2(-.3008,-.1602),vec2(-.5430,-.3633),vec2(-.3164,-.2305),
    vec2(-.5430,-.4727),vec2(-.3242,-.3711),vec2(-.1289,-.1992),vec2(-.2617,-.4727),vec2(-.2305,-.6289),
    vec2(-.0742,-.2852),vec2(-.1055,-.6055),vec2(-.0273,-.3320),vec2(.0117,-.6680),vec2(.0430,-.5977),
    vec2(.0664,-.7227),vec2(.1602,-.7617),vec2(.1680,-.6219),vec2(.1211,-.4414),vec2(.1836,-.5039),
    vec2(.1680,-.3008),vec2(.0586,-.0664),vec2(.3164,-.3555),vec2(.5039,-.4180),vec2(.5039,-.3633),
    vec2(.1477,-.0898),vec2(.3555,-.1992),vec2(.5586,-.2852),vec2(.2930,-.1289),vec2(.3555,-.1523),
    vec2(.1055,-.0430),vec2(.6758,-.1445),vec2(.6133,-.1055),vec2(.4961,-.0586),vec2(.5508,.0273),
    vec2(.3242,.0195),vec2(.2148,.0195),vec2(.0664,.0117),vec2(.2852,.0742),vec2(.1211,.0352),
    vec2(.4883,.1758),vec2(.5352,.2617),vec2(.3398,.1758),vec2(.2070,.1133),vec2(.5352,.3320),
    vec2(.2317,.1836),vec2(.4727,.3867),vec2(.1289,.1289),vec2(.1914,.2148),vec2(.3789,.4883),
    vec2(.2852,.5273),vec2(.2070,.4492),vec2(.2461,.5820),vec2(.0430,.1133),vec2(.0352,.2305),
    vec2(.0195,.2852),vec2(-.0039,.5117),vec2(-.0508,.2617),vec2(-.1576,.5430),vec2(-.0977,.2695),
    vec2(-.2227,.4648),vec2(-.0820,.1602),vec2(-.1602,.2852),vec2(-.1602,.2148),vec2(-.0742,.0820),
    vec2(-.5117,.4258),vec2(-.3232,.2305),vec2(-.3633,.1523),vec2(-.6680,.2305),vec2(-.6211,.1758),
    vec2(-.4258,.1133),vec2(-.7773,.1367),vec2(-.3086,.0195),vec2(-.5820,.0273),vec2(-.5117,.0195));

vec4 collector(vec2 pos, float radius, float angle, float time) {
    float tick = time*80.0;
    float spread = 1.0-.88*pow(max(0.0,1.0-tick/59.0),2.7);
    vec2 field = pos/spread;
    if (radius>.82*spread+.12) return vec4(0);
    float secondary = gaussian(radius/(0.066+0.02*exp(-9.0*time)))*2.8*exp(-24.0*time);
    for (int layer=0; layer<3; ++layer) {
        float count = 31.0+float(layer)*7.0, sector = (angle/TAU+0.5)*count;
        for (int offset=-1; offset<=1; ++offset) {
            float cell = floor(sector)+float(offset);
            vec2 seed = vec2(mod(cell,count), float(layer)+3.0);
            vec2 axis = direction(TAU*((cell+hash(seed))/count-0.5));
            float travel = 0.08+0.79*hash(seed+13.2);
            float along = dot(field,axis), across = dot(field,vec2(-axis.y,axis.x));
            float head = bell(along,travel,0.016+0.052*(1.0-time));
            secondary += head*gaussian(across/(0.006+0.014*exp(-5.0*time)))*(1.4-0.95*time);
        }
    }
    float energy = secondary*(1.0-smoothstep(.32,.94,time));
    float width = .0067+.014*exp(-.057*tick);
    float streak = width+.040*exp(-.048*tick);
    for (int index=0; index<80; ++index) {
        vec2 head = collectorFlight[index], delta = pos-head*spread;
        if (dot(delta,delta)>10.0*streak*streak) continue;
        vec2 axis = normalize(head);
        float along = dot(axis,delta), across = dot(vec2(-axis.y,axis.x),delta);
        energy += 1.05*gaussian(along/streak)*gaussian(across/width);
    }
    vec3 color = mix(Palette,vec3(.46,1,1),smoothstep(.9,2.5,energy));
    return vec4(mix(color,vec3(1),smoothstep(2.5,6.5,energy)),min(energy,1.0));
}

const vec3 gemWhite[48] = vec3[48](
    vec3(2,.099,27),vec3(3,.636,31),vec3(4,.264,32),vec3(17,.494,27),vec3(33,.437,27),vec3(35,.202,29),
    vec3(43,.103,29),vec3(54,.352,32),vec3(55,.486,27),vec3(61,.415,31),vec3(69,.159,32),vec3(93,.597,26),
    vec3(110,.169,31),vec3(113,.708,30),vec3(118,.712,31),vec3(132,.535,30),vec3(137,.578,28),vec3(138,.251,28),
    vec3(145,.561,30),vec3(165,.587,32),vec3(182,.103,29),vec3(188,.744,32),vec3(196,.430,31),vec3(210,.252,31),
    vec3(212,.519,33),vec3(216,.076,32),vec3(217,.433,27),vec3(222,.006,32),vec3(225,.203,32),vec3(248,.346,27),
    vec3(253,.596,28),vec3(262,.289,32),vec3(269,.198,32),vec3(286,.427,33),vec3(287,.576,27),vec3(290,.498,26),
    vec3(304,.352,32),vec3(306,.638,28),vec3(308,.524,30),vec3(316,.296,28),vec3(320,.540,26),vec3(326,.726,32),
    vec3(332,.358,30),vec3(332,.163,31),vec3(332,.414,28),vec3(346,.270,27),vec3(350,.616,27),vec3(351,.184,32));
const vec3 gemDay[48] = vec3[48](
    vec3(7,.318,27),vec3(19,.438,32),vec3(20,.368,29),vec3(22,.552,30),vec3(31,.607,29),vec3(36,.459,32),
    vec3(38,.372,33),vec3(43,.258,31),vec3(49,.322,29),vec3(58,.444,32),vec3(60,.189,27),vec3(60,.651,27),
    vec3(74,.227,30),vec3(78,.324,29),vec3(81,.126,29),vec3(81,.691,30),vec3(88,.344,30),vec3(98,.633,28),
    vec3(101,.020,32),vec3(102,.658,30),vec3(124,.302,31),vec3(136,.191,30),vec3(148,.463,27),vec3(148,.326,32),
    vec3(150,.162,30),vec3(161,.358,29),vec3(168,.299,29),vec3(168,.538,29),vec3(185,.541,30),vec3(187,.188,27),
    vec3(189,.472,29),vec3(200,.083,28),vec3(210,.712,33),vec3(216,.236,32),vec3(224,.442,27),vec3(252,.013,30),
    vec3(252,.580,32),vec3(256,.450,27),vec3(259,.621,30),vec3(267,.222,30),vec3(275,.549,28),vec3(285,.532,31),
    vec3(314,.262,32),vec3(316,.681,32),vec3(321,.317,28),vec3(322,.711,31),vec3(341,.240,27),vec3(346,.153,30));
const vec3 gemNight[48] = vec3[48](
    vec3(11,.398,32),vec3(14,.117,32),vec3(21,.624,30),vec3(24,.672,32),vec3(45,.372,32),vec3(49,.267,31),
    vec3(56,.124,31),vec3(64,.286,31),vec3(69,.534,31),vec3(72,.304,32),vec3(73,.340,31),vec3(74,.189,32),
    vec3(92,.410,31),vec3(123,.688,31),vec3(135,.028,33),vec3(138,.501,28),vec3(147,.324,29),vec3(148,.653,30),
    vec3(151,.690,30),vec3(172,.146,30),vec3(187,.661,32),vec3(195,.663,32),vec3(196,.352,31),vec3(197,.232,32),
    vec3(204,.678,31),vec3(213,.477,29),vec3(224,.328,31),vec3(230,.706,31),vec3(239,.333,31),vec3(263,.334,31),
    vec3(268,.698,30),vec3(269,.355,28),vec3(276,.701,29),vec3(279,.513,29),vec3(279,.324,29),vec3(283,.489,31),
    vec3(289,.719,32),vec3(297,.193,28),vec3(303,.703,30),vec3(304,.080,32),vec3(311,.181,29),vec3(313,.291,30),
    vec3(326,.242,32),vec3(330,.468,28),vec3(335,.316,33),vec3(346,.539,32),vec3(348,.669,31),vec3(352,.535,29));
const vec3 gemSky[48] = vec3[48](
    vec3(7,.484,27),vec3(11,.073,33),vec3(17,.293,31),vec3(27,.506,33),vec3(31,.736,31),vec3(68,.250,30),
    vec3(85,.507,32),vec3(89,.217,31),vec3(94,.578,27),vec3(98,.092,32),vec3(99,.696,30),vec3(102,.397,30),
    vec3(112,.630,31),vec3(113,.413,28),vec3(119,.269,32),vec3(136,.542,26),vec3(136,.006,32),vec3(140,.131,31),
    vec3(147,.390,29),vec3(178,.114,28),vec3(193,.418,30),vec3(198,.328,28),vec3(208,.240,31),vec3(214,.716,31),
    vec3(217,.318,29),vec3(221,.604,31),vec3(223,.106,31),vec3(223,.381,28),vec3(230,.197,27),vec3(233,.267,27),
    vec3(235,.569,33),vec3(235,.439,26),vec3(236,.680,28),vec3(245,.310,32),vec3(260,.581,29),vec3(267,.450,27),
    vec3(269,.718,30),vec3(274,.576,31),vec3(279,.273,28),vec3(282,.455,31),vec3(285,.415,30),vec3(291,.551,27),
    vec3(297,.731,32),vec3(323,.521,27),vec3(327,.083,29),vec3(329,.487,28),vec3(340,.504,26),vec3(358,.315,32));

vec4 gemBurst(vec2 pos, float radius, float time, float variant) {
    float tick = time*40.0;
    if (tick>=33.0) return vec4(0);
    float extent = 1.0-exp(-0.13*tick);
    float flash = (1.0-exp(-0.33*tick))*(1.0-smoothstep(6.0,14.0,tick));
    float energy = 4.3*gaussian(radius/(0.028+0.162*flash))*(1.0-smoothstep(7.0,15.0,tick));
    for (int index=0; index<48; ++index) {
        vec3 emitter = variant<0.5 ? gemWhite[index] : variant<1.5 ? gemDay[index] : variant<2.5 ? gemNight[index] : gemSky[index];
        float remaining = emitter.z-tick;
        if (remaining <= 0.0) continue;
        vec2 axis = direction(radians(emitter.x));
        float along = dot(pos,axis), across = dot(pos,vec2(-axis.y,axis.x));
        float reach = emitter.y*extent;
        float life = smoothstep(0.0,4.0,remaining);
        float width = (0.009+0.006*extent)*mix(.67,1.0,life);
        float streak = (0.023+0.055*emitter.y+flash*emitter.y*0.59)*mix(.65,1.0,life);
        energy += (1.06+flash)*life*smoothstep(0.0,2.0,tick)*exp(-pow(abs(across)/width,1.35)-pow(abs(along-reach)/streak,1.4));
    }
    vec3 core = variant==1.0 ? vec3(1,1,0) : variant==3.0 ? vec3(.60,1,1) : vec3(1);
    return vec4(mix(Palette,core,smoothstep(.85,2.8,energy)),min(energy,1.0));
}

float beamShaft(vec2 uv, vec3 clock, vec3 origin, vec3 lean, float bend, vec4 aperture,
                vec3 luminosity, vec3 attenuation, float curvature, float entryRate, float entryLight, float exponent) {
    float depth = uv.y;
    float axis = dot(origin,clock)+depth*(dot(lean,clock)+depth*bend);
    float width = exp(dot(aperture.xyz,clock)+depth*aperture.w);
    float shape = exp(-pow(abs(uv.x-.5-axis)/width,exponent));
    float light = exp(dot(luminosity,clock)+depth*(dot(attenuation,clock)+depth*curvature));
    return shape*light*max(0.0,entryLight+1.0-exp(-depth*entryRate));
}

vec4 lightbeam(vec2 uv, float phase, bool transfer) {
    float height = uv.y;
    vec2 orbit = direction(phase);
    float axis, aperture, falloff, exponent;
    if (transfer) {
        axis = height*(-.01474+.00623*height) + dot(orbit,vec2(-.00528,.00007)+height*vec2(.003,-.00778));
        aperture = -1.83738 + height*(.32932-.13187*height) + dot(orbit,vec2(-.0173,.00646)+height*vec2(-.02049,.0171));
        exponent = 2.92776-.06687*height;
        falloff = (.44104+1.9359*(1.0-exp(-13.33106*height)))*exp(-2.28027*height-.33396*height*height);
        float energy = falloff*exp(-pow(abs(uv.x-.5-axis)/exp(aperture),exponent));
        return vec4(1,1,1,min(energy,1.0));
    }
    vec3 clock = vec3(1,orbit);
    float energy = beamShaft(uv,clock,vec3(.02004,.02667,-.00680),vec3(.06293,-.05575,.09566),-.01580,
        vec4(-2.44371,.05359,.14147,.06635),vec3(-.13380,.15797,-.01218),vec3(-3.13598,.02829,.13966),-.56197,11.696,-.14865,1.4956)
        + beamShaft(uv,clock,vec3(.01647,-.00549,-.00070),vec3(.00820,.00544,-.01462),-.02013,
        vec4(-2.55723,.21458,-.11297,-.02757),vec3(-.54698,-.31383,.05087),vec3(-2.74432,-.15985,-.15623),-.00019,18.132,.25444,2.8553)
        + beamShaft(uv,clock,vec3(-.02592,.02360,.01270),vec3(-.03070,-.07673,.04019),.07063,
        vec4(-2.73195,.22178,-.31806,.23861),vec3(-.81547,-.11967,-.00534),vec3(-3.11138,.08874,.22112),-.74452,12.023,.24480,1.8677)
        + beamShaft(uv,clock,vec3(-.12468,-.02655,.02892),vec3(-.05110,-.01676,-.02127),.02143,
        vec4(-2.55896,-.06437,.36717,.07494),vec3(-1.26813,-.28265,-.23475),vec3(-2.52222,.71949,.00679),-.11491,7.787,.12083,2.1015)
        + beamShaft(uv,clock,vec3(.14706,-.04269,.00936),vec3(.06478,.03898,-.10689),-.00668,
        vec4(-2.52460,.02817,-.20879,-.02583),vec3(-1.49329,-.02246,.37345),vec3(-2.58568,-.14269,.08997),-.17291,7.220,.04331,1.9108);
    return vec4(vec3(.895*min(energy,1.0)),min(energy,1.0));
}

struct RelayLobe { vec4 axisOffset; vec3 shape; vec3 emission; vec4 dye; vec4 overtone; vec4 gradientAperture; };
const RelayLobe relayLobes[12] = RelayLobe[12](
    RelayLobe(vec4(.05215,-.08310,-.02183,-.00088),vec3(.13744,.03248,8.54378),vec3(-.05909,.08603,.95560),vec4(.42235,-.13223,-.07972,.01789),vec4(.21245,-.04825,-.03550,.07212),vec4(-.52255,.07643,-.49152,.29996)),
    RelayLobe(vec4(.29379,-.01592,-.02541,.00073),vec3(.04465,.02936,4.72933),vec3(.08144,.21118,-.36076),vec4(-.13336,.32481,.10525,.91578),vec4(-.03411,.00178,-.02866,-.03642),vec4(-.37330,.05560,.10608,-.28591)),
    RelayLobe(vec4(.36884,.00788,.00050,.00014),vec3(.07431,.02476,8.21128),vec3(.68857,-.58635,.15734),vec4(.49114,-.11469,-.58649,.36057),vec4(.01977,.05186,-.03942,-.12069),vec4(1.44904,-.17972,-.04613,-.21752)),
    RelayLobe(vec4(.74040,.00673,-.00707,.00062),vec3(.01767,.04499,6.54145),vec3(.59582,.28266,-.04765),vec4(.16421,.09923,.03023,.39540),vec4(-.03776,.05339,-.00327,-.01622),vec4(-.27433,.32127,.11102,.15215)),
    RelayLobe(vec4(.97106,.01599,.00388,-.00024),vec3(.06003,.02619,4.87884),vec3(.00494,-.22130,.39897),vec4(.16573,-.37548,.11496,.44172),vec4(.06488,.04164,-.02580,.02655),vec4(.29713,-.60094,-.16771,.05160)),
    RelayLobe(vec4(1.45957,-.05449,.01737,-.00116),vec3(.22184,.02802,10.53788),vec3(.47439,.36519,.15509),vec4(.38122,.01504,.20581,.06972),vec4(-.11700,.01329,.00638,-.02364),vec4(-.53460,.17476,.03594,.15425)),
    RelayLobe(vec4(1.72351,.00607,.00058,.00080),vec3(.08599,.01596,8.00318),vec3(.51450,.38596,-.38493),vec4(-.08082,.27419,.01221,1.50726),vec4(.08680,.13371,-.06432,.09688),vec4(-.86729,.77847,.04650,.08400)),
    RelayLobe(vec4(1.78436,-.01390,-.01957,.00004),vec3(.03408,.02475,3.87606),vec3(-1.24198,-.33213,.91501),vec4(.16212,-.63602,-.33229,.25194),vec4(.10999,.07507,-.17491,-.01537),vec4(.51258,.11768,-.12045,.29140)),
    RelayLobe(vec4(2.03480,.00371,.01680,-.00054),vec3(.02119,.03784,7.38071),vec3(-.10644,-.29423,-.56514),vec4(.21122,.12910,-.39025,.39455),vec4(.10077,-.05835,.06413,.03828),vec4(.48292,.59896,.12685,-.26866)),
    RelayLobe(vec4(2.38818,.00088,-.02212,-.00100),vec3(.02419,.03935,6.94617),vec3(-.37867,-.10947,.42584),vec4(.28157,-.24639,-.11966,.10608),vec4(.07895,.10090,-.02702,.04844),vec4(.18956,.26937,-.22181,-.02205)),
    RelayLobe(vec4(2.73885,.00005,-.01731,-.00051),vec3(.10671,.03282,7.37612),vec3(.63358,.31731,-.07295),vec4(.00985,.34535,.31481,1.20885),vec4(-.00567,.09476,.01325,-.02729),vec4(-.65331,-.09346,.15361,-.14300)),
    RelayLobe(vec4(2.78828,-.03322,-.01126,-.00038),vec3(.12108,.02087,8.52643),vec3(.50889,-.48003,-.35137),vec4(.28214,-.11522,-.66542,-.72387),vec4(.02959,-.15173,-.00985,-.19249),vec4(-.29453,.12958,-.12318,-.11012)));

vec4 relay(float angle, float radius, float phase) {
    vec3 clock = vec3(1,direction(phase));
    vec2 overtone = direction(2.0*phase), beams = vec2(0);
    for (int index=0; index<12; ++index) {
        RelayLobe lobe = relayLobes[index];
        float aperture = (lobe.shape.y+radius*lobe.shape.x)*clamp(1.0+dot(lobe.gradientAperture.zw,clock.yz),.5,1.5);
        float across = (radius*sin(angle-dot(lobe.axisOffset.xyz,clock))-lobe.axisOffset.w)/aperture;
        float light = exp(dot(lobe.emission,clock)+dot(lobe.overtone.xy,overtone)-radius*lobe.shape.z-across*across);
        float hue = dot(lobe.dye.xyz,clock)+dot(lobe.overtone.zw,overtone)+radius*(lobe.dye.w+dot(lobe.gradientAperture.xy,clock.yz));
        beams += light*vec2(1,hue);
    }
    float halo = exp(2.62125-pow(radius/.02418,1.01479));
    float energy = beams.x+halo;
    float dye = (beams.y+halo*(-1.40131+.55993*radius))/max(energy,.00001);
    float white = 1.48060*exp(-pow(radius/.08001,2.25210));
    return clamp(vec4(vec2(dye+white),.61844+.38245*dye+2.6*white,energy),0.0,1.0);
}

vec4 storage(vec2 uv, float phase) {
    vec2 wave = uv*(TAU*.5), orbit = direction(phase);
    float water = .79278 + cos(wave.x)*(-.02602+dot(orbit,vec2(-.02411,-.11263)))
                  + cos(2.0*wave.x)*(.08885+dot(orbit,vec2(.05539,.02876)))
                  + cos(3.0*wave.x)*dot(orbit,vec2(-.03101,.07017))
                  + cos(4.0*wave.x)*(-.04982+dot(orbit,vec2(.03235,.00964)))
                  + cos(5.0*wave.x)*(.03907+dot(orbit,vec2(-.02462,.03453)))
                  + cos(10.0*wave.x)*(.02342+dot(orbit,vec2(-.00594,-.04725)))
                  + cos(2.0*wave.y)*(.03285*cos(8.0*wave.x)-.01687)
                  - cos(wave.y)*(.02299*cos(wave.x)+.02246*cos(3.0*wave.x));
    water = clamp(water,0.0,1.0);
    float green = -3.3202+water*(12.9545+water*(-12.7783+water*4.1421));
    float opacity = -10.823+water*(42.104+water*(-49.5352+water*19.2587));
    return clamp(vec4(water,green,1,opacity),0.0,1.0);
}

void main() {
    vec2 sheet = fract(sheetUV)*Effect.xy;
    vec2 cell = floor(sheet), uv = fract(sheet), pos = 2.0*uv-1.0;
    float time = (cell.x+cell.y*Effect.x)/(Effect.x*Effect.y), phase = time*TAU;
    float radius = length(pos), angle = atan(pos.y,pos.x+1e-10);
    int kind = int(Effect.z);
    vec4 paint;
    if (kind==5 || kind==11 || kind==12) paint = ring(pos,radius,angle,time,kind);
    else if (kind==13) paint = lightbeam(uv,phase,Effect.w>0.5);
    else if (kind==14) paint = relay(angle,radius,phase);
    else if (kind==15) paint = storage(uv,phase);
    else paint = vec4(0);
    paint *= tint;
    if (paint.a < 0.1) discard;
    fragColor = paint*ColorModulator;
}
