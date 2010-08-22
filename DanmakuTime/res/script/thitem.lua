
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

THItem = {
	gravity=1,
	dx=nil,
	dy=nil,
    magnetTarget=nil,
    magnetAttrSpeed=10
	}

function THItem.new(x, y, self)
	self = extend(THItem, self or {})	
	self = THSprite.new(self)
	
	self:setColNode(0, itemColType, RectColNode.new(-8, -8, 16, 16))
	self:setPos(x or levelWidth/2, y or levelHeight/2)
	self:setZ(2000)
	self:setDrawAngleAuto(false)
	self:setAngle(256)
	self:setSpeed(self.gravity)
	
	self.dx = -4 + 8 * math.random()
	self.dy = -4 + 8 * math.random() - self.gravity
	
	return self
end

function THItem:update()
	--Cache function we're going to use for a tiny performance bonus
	local setPos = self.setPos
	local getX = self.getX
	local getY = self.getY

	while math.abs(self.dx) > 0.1 and math.abs(self.dy) > 0.1 do
		self.dx = self.dx * 0.9
		self.dy = self.dy * 0.9
	
		--self:setPos(self:getX() + self.dx, self:getY() + self.dy)
		setPos(self, getX(self) + self.dx, getY(self) + self.dy)
		
		yield()
	end
    
    while true do
        local target = self.magnetTarget
        if target ~= nil then
            if (target.deathTime or 0) > 0 then
                self.magnetTarget = nil
            else
                local ix = self:getX()
                local iy = self:getY()
                local angle = math.atan2(target:getY()-iy, target:getX()-ix)
                
                local dx = self.magnetAttrSpeed * math.sin(angle)
                local dy = self.magnetAttrSpeed * -math.cos(angle)
                
                self:setPos(ix+dx, iy+dy)
            end        
        end
        
        yield()
    end
end

function THItem:onCollision(other, myNode, otherNode)
	self:destroy()
end

-------------------------------------------------------------------------------

function dropItems(x, y, dz, tex, func, num)
	result = {}
	
	for n=1,num do
		local i = THItem.new(x, y)
		i:setZ(i:getZ() + dz)
		i:setTexture(texStore:get(tex))
		i.onCollision = func
		result[n] = i
	end
	
	return result
end

function dropPointItems(x, y, small, large)
	local smallFunc = function(self, other, myNode, otherNode)
		if otherNode:getType() == playerItemColType then
			other.points = other.points + 1
		end
		self:destroy()
	end
	local largeFunc = function(self, other, myNode, otherNode)
		if otherNode:getType() == playerItemColType then
			other.points = other.points + 5
		end
		self:destroy()
	end
	
	local t0 = dropItems(x, y, 0, "items.png#pointSmall", smallFunc, small or 1) 
	local t1 = dropItems(x, y, -1, "items.png#pointLarge", largeFunc, large or 0)
	
	return append(t0, t1)
end

function dropPowerItems(x, y, small, large)
	local smallFunc = function(self, other, myNode, otherNode)
		if otherNode:getType() == playerItemColType then
			other.shotPower = math.min(other.maxShotPower, other.shotPower + 1)
		end
		self:destroy()
	end
	local largeFunc = function(self, other, myNode, otherNode)
		if otherNode:getType() == playerItemColType then
			other.shotPower = math.min(other.maxShotPower, other.shotPower + 5)
		end
		self:destroy()
	end
	
	local t0 = dropItems(x, y, 0, "items.png#powerSmall", smallFunc, small or 1) 
	local t1 = dropItems(x, y, -1, "items.png#powerLarge", largeFunc, large or 0)

	return result
end

function dropLifeItems(x, y, num)
	local func = function(self, other, myNode, otherNode)
		if otherNode:getType() == playerItemColType then
			other.lives = math.min(other.maxLives, other.lives + 1)
		end
		self:destroy()
	end
	
	return dropItems(x, y, -5, "items.png#life", func, num or 1)
end

function dropBombItems(x, y, num)
	local func = function(self, other, myNode, otherNode)
		if otherNode:getType() == playerItemColType then
			other.bombs = math.min(other.maxBombs, other.bombs + 1)
		end
		self:destroy()
	end
	
	return dropItems(x, y, -3, "items.png#bomb", func, num or 1)
end

function dropFullRestoreItems(x, y, num)
	local func = function(self, other, myNode, otherNode)
		if otherNode:getType() == playerItemColType then
			other.shotPower = other.maxShotPower
		end
		self:destroy()
	end
	
	return dropItems(x, y, -3, "items.png#fullRestore", func, num or 1)
end

function dropMagnetItems(x, y, num)
	local func = function(self, other, myNode, otherNode)
		if otherNode:getType() == playerItemColType then
			other.points = other.points + 1
		end
		self:destroy()
	end
	
	local items = dropItems(x, y, -3, "items.png#magnet", func, num or 1)
    for _,item in ipairs(items) do
        local p = getClosestPlayer(item:getX(), item:getY())
        if p ~= nil then
            item.magnetTarget = p
        end
    end
    return items
end

-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
