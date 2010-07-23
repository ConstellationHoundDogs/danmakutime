
ParamText = {
	object=nil,
	param=nil,
	label=nil
	}

function ParamText.new(field, obj, paramName, lbl, x, y, blockAnchor, ww)
	local self = extend(ParamText, {object=obj, param=paramName, label=lbl})
	self = TextDrawable.new(field, self)
	self:setPos(x or 0, y or 0)
	self:setBlockAnchor(blockAnchor or 7)
	if ww ~= nil then
		self:setWidth(ww)
	end
	
	self:setFontName("DejaVuSans") --fontname is the file name without extension
	self:setFontStyle(FontStyle.BOLD)
	self:setFontSize(12)
	self:setOutlineColor(.1, .1, .1)
	self:setOutlineSize(1.5)
	return self
end

function ParamText:update()
	while true do
		local value = tostring(self.object[self.param])
		
		self:setText(self.label .. ": " .. value)
		yield()
	end
end
