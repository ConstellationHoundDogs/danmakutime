
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-- 
-- External dependencies:
--
-- THSprite
-- player
-- itemColType
-- playerItemColType
-- 
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

THItem = {
	gravity=1,
	dx=nil,
	dy=nil
	}

function THItem.new(x, y, self)
	self = extend(THItem, self or {})	
	self = THSprite.new(self)
	
	self:setColNode(0, itemColType, RectColNode.new(-8, -8, 16, 16))
	self:setPos(x or levelWidth/2, y or levelHeight/2)
	self:setZ(player:getZ() + 50)
	
	self.dx = -4 + 8 * math.random()
	self.dy = -4 + 8 * math.random() - self.gravity
	
	return self
end

function THItem:update()
	--Cache function we're going to use for a tiny performance bonus
	local setPos = self.setPos
	local getX = self.getX
	local getY = self.getY

	while true do
		self.dx = self.dx * 0.9
		self.dy = self.dy * 0.9
	
		--self:setPos(self:getX() + self.dx, self:getY() + self.dy + self.gravity)
		setPos(self, getX(self) + self.dx, getY(self) + self.dy + self.gravity)
		
		yield()
	end
end

function THItem:onCollision(other, myNode, otherNode)
	self:destroy()
end

-------------------------------------------------------------------------------

function dropPointItems(x, y, small, large, amplitudeX, amplitudeY)
	small = small or 0
	large = large or 0
	
	result = {}
	
	for n=1,(small+large) do
		local i = THItem.new(x, y)
		if n <= small then
			i:setTexture(texStore:get("items.png#pointSmall"))
			i.onCollision = function(self, other, myNode, otherNode)
				if otherNode:getType() == playerItemColType then
					other.points = other.points + 1
				end
				self:destroy()
			end
		else
			i:setTexture(texStore:get("items.png#pointLarge"))
			i.onCollision = function(self, other, myNode, otherNode)
				if otherNode:getType() == playerItemColType then
					other.points = other.points + 10
				end
				self:destroy()
			end
		end
		result[n] = i
	end
	
	return result
end

-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
-------------------------------------------------------------------------------
