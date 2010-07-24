
function buildLevel()	
	--Create some text
	local text = TextDrawable.new(overlayField)
	text:setText("Danmaku Time\nDay 5")
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
	local ww = screenWidth - paramX - 20
	
	ParamText.new(overlayField, player, "lives", "Lives: ", paramX, paramY, 7, ww)
	paramY = paramY + paramDY
	ParamText.new(overlayField, player, "bombs", "Bombs: ", paramX, paramY, 7, ww)
	paramY = paramY + paramDY
	ParamText.new(overlayField, player, "shotPower", "Power: ", paramX, paramY, 7, ww)
	paramY = paramY + paramDY
	ParamText.new(overlayField, player, "grazeCounter", "Graze: ", paramX, paramY, 7, ww)
	paramY = paramY + paramDY
	ParamText.new(overlayField, player, "points", "Points: ", paramX, paramY, 7, ww)
end
