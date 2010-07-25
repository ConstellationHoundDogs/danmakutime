
function buildLevel()
	--Create background
	scrollingBackground(texStore:get("bgscroll.png"), .01, 2)
	
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

scrollingBackgroundTiles = {}

function scrollingBackground(tex, dx, dy)
	for i,v in ipairs(scrollingBackgroundTiles) do
		v:destroy()
	end
	scrollingBackgroundTiles = {}

	local tw = tex:getWidth()
	local th = tex:getHeight()
	
	local minX = -tw/2
	local minY = -th/2
	local maxX = levelWidth + tw/2
	local maxY = levelHeight + th/2
	
	local reqImagesX = math.ceil((maxX-minX) / tw)
	local reqImagesY = math.ceil((maxY-minY) / th)
		
	for x=1,reqImagesX do
		for y=1, reqImagesY do
			local d = Drawable.new(gameField)
			d:setPos(x * tw, y * th)			
			d:setZ(32001)
			d:setTexture(tex)
			d.update = function(self)
				while true do
					local x = d:getX() + dx
					local y = d:getY() + dy

					if x > maxX then x = x - (maxX - minX) end
					if y > maxY then y = y - (maxY - minY) end
					
					d:setPos(x, y)
					yield()
				end
			end
			
			table.insert(scrollingBackgroundTiles, d)
		end
	end		
end
