
function buildLevel()
	--Create background
	scrollingBackground(texStore:get("bgscroll.png"), .01, 2)
	
    players = {}
    
	--Create player(s)
	players[1] = THPlayer.new()
	--players[1]:setColNode(999, playerGrazeColType, LineSegColNode.new(0, 0, 0, -100, 1))
    
    local p2controls = {
        up=Keys.I, down=Keys.K, left=Keys.J, right=Keys.L,
        fire=Keys.G, bomb=Keys.H, focus=Keys.F
    }
	players[2] = THPlayer.new{controls=p2controls}
    
    local p3controls = {
        up=Keys.NUMPAD5, down=Keys.NUMPAD2, left=Keys.NUMPAD1, right=Keys.NUMPAD3,
        fire=Keys.NUMPAD4, bomb=Keys.NUMPAD0, focus=Keys.NUMPAD7
    }
	players[3] = THPlayer.new{controls=p3controls}
    
    local p4controls = {
        up=Keys.HOME, down=Keys.END, left=Keys.DELETE, right=Keys.PAGE_DOWN,
        fire=Keys.INSERT, bomb=Keys.PAGE_UP, focus=Keys.BACK_SPACE
    }
	players[4] = THPlayer.new{controls=p4controls}

	--Create OSD
	local paramX = gameField:getX() + gameField:getWidth() + 10
	local paramY = gameField:getY() + 10 + 20
	local ww = screenWidth - paramX - 20
	
    paramY = createPlayerStatsDisplay(paramX, paramY, ww, 7, players[1])
    if #players >= 2 then
        paramY = paramY + 30
        paramY = createPlayerStatsDisplay(paramX, paramY, ww, 7, players[2])
        if #players >= 3 then
            paramY = paramY + 30
            paramY = createPlayerStatsDisplay(paramX, paramY, ww, 7, players[3])
            if #players >= 4 then
                paramY = paramY + 30
                paramY = createPlayerStatsDisplay(paramX, paramY, ww, 7, players[3])
            end
        end
    end
end

function createPlayerStatsDisplay(x, y, w, align, player)
	local dy = 15

	ParamText.new(overlayField, player, "lives", "Lives: ", x, y, align, w)
	y = y + dy
	ParamText.new(overlayField, player, "bombs", "Bombs: ", x, y, align, w)
	y = y + dy
	ParamText.new(overlayField, player, "shotPower", "Power: ", x, y, align, w)
	y = y + dy
	ParamText.new(overlayField, player, "grazeCounter", "Graze: ", x, y, align, w)
	y = y + dy
	ParamText.new(overlayField, player, "points", "Points: ", x, y, align, w)
	y = y + dy
    
    return y
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
