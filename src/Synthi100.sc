Synthi100 {
	var <server; // Servidor por defecto

	// Módulos que incluye:
	var <modulOscillators;
	var <modulInputAmplifiers;
	var <modulOutputChannels;
	var <modulPatchbayAudio;
	var <conectionOut;

	// Buses internos de entrada y salida:
	var <audioInBuses;
	var <audioOutBuses;

	// Buses externos de entrada y salida:
	var <stereoOutBuses;

	// Array de direcciones a la que enviar los mensajes OSC
	var netAddr;

	// Diccionario que almacena los dispositivos OSC con los que se comunica Synthi100
	var oscDevices = nil;

	// Opciones:
	classvar numAudioInBuses = 8;
	classvar numAudioOutBuses = 8;

	// Estado del Synthi100
	var play = false;



	// Métodos de clase //////////////////////////////////////////////////////////////////

	*initClass {
		// Inicializa otras clases antes de esta
		Class.initClassTree(S100_Oscillator);
		Class.initClassTree(S100_OutputChannel);
		Class.initClassTree(S100_InputAmplifier);
		Class.initClassTree(S100_PatchbayAudio);
	}

	*new { arg server = Server.local, stereoBuses = [0,1];
		^super.new.init(server, stereoBuses);
	}



	// Métodos de instancia //////////////////////////////////////////////////////////////

	init {|serv, stereoBuses|
		// Se añaden al servidor las declaracines SynthDefs
		S100_Oscillator.addSynthDef;
		S100_InputAmplifier.addSynthDef;
		S100_OutputChannel.addSynthDef;
		S100_PatchbayAudio.addSynthDef;
		server = serv;
		NetAddr.broadcastFlag = true;

		// Buses de audio de entrada y salida
		audioInBuses = numAudioInBuses.collect({Bus.audio(server, 1)});
		audioOutBuses = numAudioOutBuses.collect({Bus.audio(server, 1)});
		stereoOutBuses = stereoBuses;

		// Módulos
		modulOscillators = 12.collect({S100_Oscillator(serv)});
		modulInputAmplifiers = 8.collect({S100_InputAmplifier(serv)});
		modulOutputChannels = 8.collect({|i| S100_OutputChannel(serv, modulInputAmplifiers[i].outputBus)});
		modulPatchbayAudio = S100_PatchbayAudio(server);
	}


	// Métodos de instancia /////////////////////////////////////////////////////////////////////////

	run {
		if (play == true, {("Create a new instance of " ++ this.class).postln; ^0}, {
			server.waitForBoot({
				// TODO: Crear alguna rutina para colocar en orden todos los Synths en el servidor. Ahora están colocados mezclados los osciladores y los conectores.

				// Rutina para espaciar temporalmente la creacion de cada Synth, de forma que queden ordenados.
				Routine({
					var waitTime = 0.01; // Tiempo de espera entre la creación de cada Synth

					// Se conectan las salidas de los canales de salida a los dos primeros buses de salida especificados en stereoOutBuses
					conectionOut = modulOutputChannels.collect({|i|
						SynthDef(\conection, {
							Out.ar(stereoOutBuses[0], In.ar(i.outBusL, 1));
							Out.ar(stereoOutBuses[1], In.ar(i.outBusR, 1));
						}).play(server);
						wait(waitTime);
					});
					wait(waitTime);

					// Se arrancan todos los Synths de todos los módulos //////////////////////////////////

					// Output Channels
					modulOutputChannels.do({|i|
						i.createSynth;
						wait(waitTime);
					});
					wait(waitTime);

					// Input Amplifier
					modulInputAmplifiers.do({|i|
						i.createSynth;
						wait(waitTime);
					});
					wait(waitTime);

					// Oscillators
					modulOscillators.do({|i|
						i.createSynth;
						wait(waitTime);
					});
					wait(waitTime);
					modulPatchbayAudio.connect(modulOscillators, modulInputAmplifiers, modulOutputChannels);
					wait(waitTime);
				}).play;
				play = true;
				"Synthi100 running!!".postln;
			})
		});
	}

	// Habilita el envío y recepción de mensajes OSC desde otros dispositivos.
	connectOSC {
		var routineSearchOSC = Routine({
			var functionOSC = {|msg, time, addr, recvPort|
				if("/S100/sync".matchRegexp(msg[0].asString), {
					if(oscDevices.trueAt(addr.ip) == false, {
						oscDevices.put(addr.ip, addr.ip);
						("Found device at " ++ addr.ip).postln;
					})
				})
			};
			"Searching OSC devices...".postln;
			oscDevices = Dictionary.new;
			thisProcess.addOSCRecvFunc(functionOSC);
			wait(5);
			thisProcess.removeOSCRecvFunc(functionOSC);
			("Found " ++ oscDevices.size ++ " devices:").postln;
			oscDevices.do({|i| i.postln});
			if (oscDevices.size > 0, {
				this.prepareOSC;
				netAddr = List.new;
				oscDevices.do({|i|
					netAddr.add(NetAddr(i, 9000));
				});
				this.sendStateOSC
			});
		});
		routineSearchOSC.play;
	}


	prepareOSC {
		var functionOSC = {|msg, time, addr, recvPort|
			if(
				"/osc".matchRegexp(msg[0].asString).or({
					"/out".matchRegexp(msg[0].asString).or({
						"/patchATouchOSC".matchRegexp(msg[0].asString)
					})
				}), {
					// reenvía en broadcasting el mensaje recibido para que otros dispositivos se puedan sicronizar con los cambios realizados en cualquiera de ellos.
					//	this.setParameterOSC(msg[0], msg[1]);
					this.setParameterOSC(msg[0].asString, msg[1]);
					// Se envía el mismo mensaje a todas las direcciones menos a la remitente
					netAddr.do({|i|
						if(addr.ip != i.ip, {
							i.sendMsg(msg[0], msg[1])
						})
					})
			});
		};
		thisProcess.removeOSCRecvFunc(functionOSC);
		netAddr = NetAddr("255.255.255.255", 9000); // el puerto 9000 es por el que se enviarán los mensajes OSC
		thisProcess.addOSCRecvFunc(functionOSC);
	}


	// Envía el estado de todo el Synthi por OSC
	// Para mejorarlo sería bueno mandar un bundle.
	sendStateOSC {
		/*		// Buscando cómo enviar un bundle con todos los mensajes OSC juntos...

		var bundle = List.new;
		14.do({|i|
		6.do({|j|
		var string = "/patchATouchOSC/" ++ (i + 1) ++ "/" ++ (j + 1);
		bundle.add(string);
		bundle.add(0);
		})
		});

		this.getState.do({|msg|
		bundle.add(msg[0]);
		bundle.add(msg[1]);
		});

		netAddr.do({|i| i.sendBundle(nil, bundle.asArray).postln});

		*/

		Routine({
			// ponemos los pines de Pathbay de audio a 0
			14.do({|i|
				6.do({|j|
					var string = "/patchATouchOSC/" ++ (i + 1) ++ "/" ++ (j + 1);
					netAddr.do({|i| i.sendMsg(string, 0)});
					wait(0.03);
				})
			});
			this.getState.do({|msg|
				wait(0.03);
				netAddr.do({|i| i.sendMsg(msg[0], msg[1])})
			})
		}).play;


	}

	// Setter de los diferentes parámetros de los módulos en formato OSC
	setParameterOSC {|string, value|
		var splitted = string.split($/);
		switch (splitted[1],
			"osc", { // Ejemplo: "/osc/1/pulse/level"
				var index = splitted[2].asInt - 1;
				var parameter;
				3.do({splitted.removeAt(0)});
				if (splitted.size == 1,
					{parameter = splitted[0]},
					{parameter = splitted[0]++splitted[1]}
				);
				switch (parameter,
					"range", {modulOscillators[index].setRange(value)},
					"frequency", {modulOscillators[index].setFrequency(value)},
					"pulselevel", {modulOscillators[index].setPulseLevel(value)},
					"pulseshape", {modulOscillators[index].setPulseShape(value)},
					"sinelevel", {modulOscillators[index].setSineLevel(value)},
					"sinesymmetry", {modulOscillators[index].setSineSymmetry(value)},
					"trianglelevel", {modulOscillators[index].setTriangleLevel(value)},
					"sawtoothlevel", {modulOscillators[index].setSawtoothLevel(value)}
				)
				//	modulOscillators[index].setParameter(parameter, value);
			},
			"patchA", { // Ejemplo "/patchA/91/36". Origen de coordenadas izquierda arriba / Orden: vertical y horzontal
				2.do({splitted.removeAt(0)});
				modulPatchbayAudio.administrateNode(splitted[0].asInt, splitted[1].asInt, value);
			},
			// Mensaje DE PRUEBAS para TouchOSC con 3 osciladores
			"patchATouchOSC", { // Patchbay de Audio desde TouchOSC, cuyo origen de coordenadas es izquierda abajo. Orden: horizontal y vertical (como un sistema normal de coordenadas)
				var x, y;
				var translation = Dictionary.new;
				// Los tres primeros osciladores:
				translation.put(1, 96);
				translation.put(2, 95);
				translation.put(3, 94);
				translation.put(4, 93);
				translation.put(5, 92);
				translation.put(6, 91);
				// Los cuatro primeros outputs channels:
				translation.put(7, 78);
				translation.put(8, 77);
				translation.put(9, 76);
				translation.put(10, 75);
				// Los cuatro primeros inputs de los amplificadores (outputs channels):
				translation.put(11, 70);
				translation.put(12, 69);
				translation.put(13, 68);
				translation.put(14, 67);
				2.do({splitted.removeAt(0)});
				y = translation[splitted[0].asInt];
				x = splitted[1].asInt + 35;

				modulPatchbayAudio.administrateNode(y, x, value);
			},
			"out", { // Ejemplo "/out/1/level"
				var index = splitted[2].asInt - 1;
				3.do({splitted.removeAt(0)});
				switch (splitted[0],
					"level", {modulOutputChannels[index].setLevel(value)},
					"filter", {modulOutputChannels[index].setFilter(value)},
					"on", {
						modulInputAmplifiers[index].setOn(value);
						modulOutputChannels[index].setOn(value)
					},
					"pan", {modulOutputChannels[index].setPan(value)},
				)
			},
		);
	}

	// Devuelve una colección de pares [mensaje_OSC, valor] con el estado actual de todos los módulos
	getState {
		var data = List.newClear(0);

		// Oscillators:
		modulOscillators.do({|osc, num|
			var string = "/osc/" ++ (num + 1) ++ "/";
			data.add([string ++ "range", osc.range]);
			data.add([string ++ "pulse/level", osc.pulseLevel]);
			data.add([string ++ "pulse/shape", osc.pulseShape]);
			data.add([string ++ "sine/level", osc.sineLevel]);
			data.add([string ++ "sine/symmetry", osc.sineSymmetry]);
			data.add([string ++ "triangle/level", osc.triangleLevel]);
			data.add([string ++ "sawtooth/level", osc.sawtoothLevel]);
			data.add([string ++ "frequency", osc.frequency]);
		});

		// Output channels:
		modulOutputChannels.do({|oc, num|
			var string = "/out/" ++ (num + 1) ++ "/";
			data.add([string ++ "filter", oc.filter]);
			data.add([string ++ "pan", oc.pan]);
			data.add([string ++ "on", oc.on]);
			data.add([string ++ "level", oc.level]);
		});

		// Patchbay Audio:
		modulPatchbayAudio.nodeSynths.do({|node|
			var ver = node[\coordenates][0];
			var hor = node[\coordenates][1];
			data.add("/patchA/" ++ ver ++ "/" ++ hor);
		});

		// Patchbay Audio (Para pruebas con TouchOSC):
		modulPatchbayAudio.nodeSynths.do({|node|
			var ver = node[\coordenates][0];
			var hor = node[\coordenates][1];
			var translation = Dictionary.new;
			// Los tres primeros osciladores:
			translation.put(96, 1);
			translation.put(95, 2);
			translation.put(94, 3);
			translation.put(93, 4);
			translation.put(92, 5);
			translation.put(91, 6);
			// Los cuatro primeros outputs channels:
			translation.put(78, 7);
			translation.put(77, 8);
			translation.put(76, 9);
			translation.put(75, 10);
			// Los cuatro primeros inputs de los amplificadores (outputs channels):
			translation.put(70, 11);
			translation.put(69, 12);
			translation.put(68, 13);
			translation.put(67, 14);
			data.add("/patchATouchOSC/" ++ translation[ver] ++ "/" ++ hor-35);
		});

		^data;
	}
}
