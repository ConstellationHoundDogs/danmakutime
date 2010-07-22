
levelWidth = 384
levelHeight = 448

function buildLevel(background)
	--Setup collision matrix
	local colMatrix = ColMatrix.new()
	playerColType = colMatrix:newColType()
	playerGrazeColType = colMatrix:newColType()	
	playerShotColType = colMatrix:newColType()	
	enemyColType = colMatrix:newColType()	
	enemyShotColType = colMatrix:newColType()	
	colMatrix:setColliding(playerColType, enemyColType)
	colMatrix:setColliding(playerColType, enemyShotColType)
	colMatrix:setColliding(playerGrazeColType, enemyColType)
	colMatrix:setColliding(playerGrazeColType, enemyShotColType)
	colMatrix:setColliding(playerShotColType, enemyColType)
	colMatrix:setColliding(enemyColType, playerShotColType)

	--Create the game area (id=1)
	gameField = Field.new(1,
		(screenWidth-levelWidth)/2, (screenHeight-levelHeight)/2,
		levelWidth, levelHeight, 32)
	
	gameColField = gameField:getColField()	
	gameColField:setColMatrix(colMatrix)

	--Create background image
	local levelBG = Drawable.new(0)
	levelBG:setTexture(texStore:get(background))
	levelBG:setPos(screenWidth/2, screenHeight/2)
	levelBG:setZ(32000)

	--Create the overlay field (id=999)
	overlayField = Field.new(999, 0, 0, screenWidth, screenHeight, 0)
	
	--Create some text
	local text = TextDrawable.new(overlayField)
	text:setText("Danmaku Time\nDay 4")
	text:setBlockAnchor(7)
	text:setFontName("DejaVuSans") --fontname is the file name without extension
	--text:setFontStyle(FontStyle.BOLD)
	text:setFontSize(14)
	text:setPos(0, -2)
	
	--Create player
	player = THPlayer.new()
	--player:setColNode(999, playerGrazeColType, LineSegColNode.new(0, 0, 0, -100, 1))
	
	--Create OSD
	local paramX = gameField:getX() + gameField:getWidth() + 10
	local paramY = gameField:getY() + 10 + 50
	local paramDY = 15
	
	ParamText.new(overlayField, player, "lives", "Lives", paramX, paramY)
	paramY = paramY + paramDY
	ParamText.new(overlayField, player, "bombs", "Bombs", paramX, paramY)
	paramY = paramY + paramDY
	ParamText.new(overlayField, player, "grazeCounter", "Graze", paramX, paramY)
end
