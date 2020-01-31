S100_GUIPannel1 : S100_GUIPannel {
	makeWindow {
		var rect;
		var image;
		super.makeWindow;
		rect = Rect(
			left: 0,
			top: Window.availableBounds.height/1.9,
			width: window.bounds.width,
			height: window.bounds.height,
		);
		window.name = "Panel 1";
		window.bounds = rect;
		image = Image(installedPath ++ "/classes/GUI/images/pannel_1.png");
		compositeView.setBackgroundImage(image,10);
		// Cuando se hace doble click se hace zoom
		compositeView.mouseDownAction_({|view, x, y, modifiers, buttonNumber, clickCount|
			var factor = 1.5;
			if(clickCount == 2, {
				buttonNumber.switch(
					0, {this.resizePannel(factor)},
					1, {this.resizePannel(1/factor)},
				)
			})
		});
		window.front;
	}
}