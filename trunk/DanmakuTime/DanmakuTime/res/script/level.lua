
levelWidth = 384
levelHeight = 448

function buildLevel(background)
	local z = 32000;

	--Create the game area
	Field.new(1,
		(screenWidth-levelWidth)/2, (screenHeight-levelHeight)/2,
		levelWidth, levelHeight, 64)

	--Create background image
	local levelBG = Drawable.new()
	levelBG:setTexture(textureStore:getTexture(background))
	levelBG:setPos(levelWidth/2, levelHeight/2)
	levelBG:setZ(z)
	
	--Create some text
	local text = TextDrawable.new()
	text:setText("Testing, 1, 2, 3")
	text:setPos(100, 100)
	text:setDrawAngle(128)
	text:setZ(-1000)
	
	--Create player
	player = Player.new()
		
end
