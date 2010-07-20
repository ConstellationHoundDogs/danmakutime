
levelWidth = 384
levelHeight = 448

function buildLevel(background)
	--Create the game area (id=1)
	gameField = Field.new(1,
		(screenWidth-levelWidth)/2, (screenHeight-levelHeight)/2,
		levelWidth, levelHeight, 64)

	--Create background image
	local levelBG = Drawable.new(0)
	levelBG:setTexture(textureStore:getTexture(background))
	levelBG:setPos(screenWidth/2, screenHeight/2)
	levelBG:setZ(32000)

	--Create the overlay field (id=2)
	overlayField = Field.new(2, 0, 0, screenWidth, screenHeight, 0)
	
	--Create some text
	local text = TextDrawable.new(overlayField)
	text:setText("Testing, 1, 2, 3")
	text:setPos(100, 100)
	text:setDrawAngle(128)
	
	--Create player
	player = Player.new()
		
end
